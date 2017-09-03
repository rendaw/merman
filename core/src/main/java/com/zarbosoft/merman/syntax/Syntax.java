package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.Walk;
import com.zarbosoft.luaconf.LuaConf;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.serialization.Load;
import com.zarbosoft.merman.modules.Module;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolText;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.reflections.Reflections;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.stream;

@Configuration
public class Syntax {

	@Configuration()
	public String name;

	@Configuration
	public static enum BackType {
		@Configuration(name = "luxem")
		LUXEM,
		@Configuration(name = "json")
		JSON
	}

	@Configuration(name = "type", optional = true)
	public BackType backType = BackType.LUXEM;

	@Configuration(optional = true)
	public ModelColor background = ModelColor.RGB.white;

	@Configuration(optional = true, name = "pad")
	public Padding pad = new Padding();

	@Configuration(optional = true)
	public String placeholder = "▢";

	@Configuration(optional = true)
	public List<Style> styles = new ArrayList<>();

	@Configuration(optional = true, name = "banner_pad")
	public Padding bannerPad = new Padding();
	@Configuration(optional = true, name = "detail_pad")
	public Padding detailPad = new Padding();

	@Configuration(optional = true, name = "detail_span")
	public int detailSpan = 300;

	@Configuration
	public List<FreeAtomType> types = new ArrayList<>();

	@Configuration(optional = true)
	public Map<String, java.util.Set<String>> groups = new HashMap<>();

	@Configuration
	public RootAtomType root;

	@Configuration(optional = true)
	public GapAtomType gap = new GapAtomType();
	@Configuration(name = "prefix_gap", optional = true)
	public PrefixGapAtomType prefixGap = new PrefixGapAtomType();
	@Configuration(name = "suffix_gap", optional = true)
	public SuffixGapAtomType suffixGap = new SuffixGapAtomType();
	@Configuration(name = "gap_placeholder", optional = true)
	public Symbol gapPlaceholder = new SymbolText("•");
	@Configuration(optional = true, name = "gap_choice_style")
	public BoxStyle gapChoiceStyle = new BoxStyle();

	@Configuration(optional = true)
	public List<Module> modules = new ArrayList<>();

	@Configuration(optional = true, name = "animate_course_placement")
	public boolean animateCoursePlacement = false;
	@Configuration(optional = true, name = "animate_details")
	public boolean animateDetails = false;

	@Configuration(optional = true, name = "start_windowed")
	public boolean startWindowed = false;

	@Configuration(optional = true, name = "ellipsize_threshold")
	public int ellipsizeThreshold = Integer.MAX_VALUE;

	@Configuration(optional = true, name = "lay_brick_batch_size")
	public int layBrickBatchSize = 10;

	@Configuration(optional = true, name = "retry_expand_factor")
	public double retryExpandFactor = 1.25;

	@Configuration(optional = true, name = "scroll_factor")
	public double scrollFactor = 0.1;

	@Configuration(optional = true, name = "scroll_alot_factor")
	public double scrollAlotFactor = 0.8;

	@Configuration(optional = true, name = "pretty_save")
	public boolean prettySave = false;

	public String id; // Fake final - don't modify (set in loadSyntax)

	@Configuration
	public enum Direction {
		@Configuration(name = "up")
		UP,
		@Configuration(name = "down")
		DOWN,
		@Configuration(name = "left")
		LEFT,
		@Configuration(name = "right")
		RIGHT
		// TODO boustrophedon
	}

	@Configuration(name = "converse_direction", optional = true)
	public Direction converseDirection = Direction.RIGHT;

	@Configuration(name = "transverse_direction", optional = true)
	public Direction transverseDirection = Direction.DOWN;

	Grammar grammar;

	public static Reflections reflections = new Reflections("com.zarbosoft");

	public static Syntax loadSyntax(final String id, final Path path) {
		final Syntax out = LuaConf.parse(reflections, new Walk.TypeInfo(Syntax.class), path);
		out.id = id;
		out.finish();
		return out;
	}

	public void finish() {
		// jfx, qt, and swing don't support vertical languages
		if (!ImmutableSet.of(Direction.LEFT, Direction.RIGHT).contains(converseDirection) ||
				(transverseDirection != Direction.DOWN))
			throw new InvalidSyntax("Currently only converse directions left/right and transverse down are supported.");
		switch (converseDirection) {
			case LEFT:
			case RIGHT:
				switch (transverseDirection) {
					case LEFT:
					case RIGHT:
						throw new InvalidSyntax("Secondary direction must cross converse direction axis.");
				}
				break;
			case UP:
			case DOWN:
				switch (transverseDirection) {
					case UP:
					case DOWN:
						throw new InvalidSyntax("Secondary direction must cross converse direction axis.");
				}
				break;
		}

		{
			final Deque<Pair<PSet<String>, Iterator<String>>> stack = new ArrayDeque<>();
			stack.addLast(new Pair<>(HashTreePSet.empty(), groups.keySet().iterator()));
			while (!stack.isEmpty()) {
				final Pair<PSet<String>, Iterator<String>> top = stack.pollLast();
				if (!top.second.hasNext())
					continue;
				final String childKey = top.second.next();
				final Set<String> child = groups.get(childKey);
				if (child == null)
					continue;
				if (top.first.contains(childKey))
					throw new InvalidSyntax(String.format("Circular reference in group [%s].", childKey));
				stack.addLast(top);
				stack.addLast(new Pair<>(top.first.plus(childKey), child.iterator()));
			}
		}

		final Set<String> scalarTypes = new HashSet<>(); // Types that only have one back element
		final Set<String> allTypes = new HashSet<>();
		for (final FreeAtomType t : types) {
			if (t.back.isEmpty())
				throw new InvalidSyntax(String.format("Type [%s] has no back parts.", t.id()));
			if (allTypes.contains(t.id()))
				throw new InvalidSyntax(String.format("Multiple types with id [%s].", t.id()));
			allTypes.add(t.id());
			if (t.back.size() == 1)
				scalarTypes.add(t.id());
		}
		final Map<String, Set<String>> groupsThatContainType = new HashMap<>();
		final Set<String> potentiallyScalarGroups = new HashSet<>();
		for (final Map.Entry<String, Set<String>> pair : groups.entrySet()) {
			final String group = pair.getKey();
			for (final String child : pair.getValue()) {
				if (!allTypes.contains(child) && !groups.containsKey(child))
					throw new InvalidSyntax(String.format("Group [%s] refers to non-existant member [%s].",
							group,
							child
					));
				groupsThatContainType.putIfAbsent(child, new HashSet<>());
				groupsThatContainType.get(child).add(pair.getKey());
			}
			if (allTypes.contains(group))
				throw new InvalidSyntax(String.format("Group id [%s] already used.", group));
			allTypes.add(group);
			potentiallyScalarGroups.add(group);
		}
		for (final FreeAtomType t : types) {
			if (t.back.size() == 1)
				continue;
			final Deque<Iterator<String>> stack = new ArrayDeque<>();
			stack.add(groupsThatContainType.getOrDefault(t.id(), ImmutableSet.of()).iterator());
			while (!stack.isEmpty()) {
				final Iterator<String> top = stack.pollLast();
				if (top.hasNext()) {
					stack.addLast(top);
					final String notScalarGroup = top.next();
					if (potentiallyScalarGroups.contains(notScalarGroup)) {
						stack.add(groupsThatContainType.getOrDefault(notScalarGroup, ImmutableSet.of()).iterator());
						potentiallyScalarGroups.remove(notScalarGroup);
					}
				}
			}
		}
		scalarTypes.addAll(potentiallyScalarGroups);
		for (final FreeAtomType t : types) {
			t.finish(this, allTypes, scalarTypes);
		}
		root.finish(this, allTypes, scalarTypes);
		gap.finish(this, allTypes, scalarTypes);
		prefixGap.finish(this, allTypes, scalarTypes);
		suffixGap.finish(this, allTypes, scalarTypes);
	}

	public Node backRuleRef(final String type) {
		final Union out = new Union();
		out.add(new Reference(type));
		out.add(new Reference("__gap"));
		out.add(new Reference("__prefix_gap"));
		out.add(new Reference("__suffix_gap"));
		return out;
	}

	public Grammar getGrammar() {
		if (grammar == null) {
			grammar = new Grammar();
			types.forEach(t -> grammar.add(t.id(), t.buildBackRule(this)));
			grammar.add(gap.id(), gap.buildBackRule(this));
			grammar.add(prefixGap.id(), prefixGap.buildBackRule(this));
			grammar.add(suffixGap.id(), suffixGap.buildBackRule(this));
			groups.forEach((k, v) -> {
				final Union group = new Union();
				v.forEach(n -> group.add(new Reference(n)));
				grammar.add(k, group);
			});
			grammar.add("root", root.buildBackRule(this));
		}
		return grammar;
	}

	public Document create() {
		return new Document(this, root.create(this));
	}

	public Document load(final Path path) throws FileNotFoundException, IOException {
		try (
				InputStream data = Files.newInputStream(path)
		) {
			return load(data);
		}
	}

	public Document load(final String string) {
		return load(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	public Document load(final InputStream data) {
		return Load.load(this, data);
	}

	public FreeAtomType getType(final String type) {
		return types.stream().filter(t -> t.id().equals(type)).findFirst().get();
	}

	public Stream<FreeAtomType> getLeafTypes(final String type) {
		if (type == null)
			return types.stream(); // Gap types
		final Set<String> group = groups.get(type);
		if (group == null)
			return Stream.of(getType(type));
		final Deque<Iterator<String>> stack = new ArrayDeque<>();
		stack.addLast(group.iterator());
		return stream(new Iterator<FreeAtomType>() {
			@Override
			public boolean hasNext() {
				return !stack.isEmpty();
			}

			@Override
			public FreeAtomType next() {
				final Iterator<String> top = stack.pollLast();
				if (!top.hasNext())
					return null;
				final String childKey = top.next();
				if (top.hasNext())
					stack.addLast(top);
				final Set<String> child = groups.get(childKey);
				if (child == null) {
					return getType(childKey);
				} else {
					stack.addLast(child.iterator());
				}
				return null;
			}
		}).filter(x -> x != null);
	}
}
