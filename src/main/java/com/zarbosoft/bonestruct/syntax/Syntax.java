package com.zarbosoft.bonestruct.syntax;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.syntax.front.RootFrontDataArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.modules.Module;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.events.InterfaceEvent;
import com.zarbosoft.luaconf.LuaConf;
import com.zarbosoft.luxem.read.LuxemEvent;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Pair;
import javafx.scene.paint.Color;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.stream;

@Configuration
public class Syntax {

	@Configuration(description = "The name of the syntax.  This may be used in menus and dialogs.")
	public String name;

	@Configuration(optional = true, description = "The background color of the document.")
	public Color background = Color.WHITE;

	@Configuration(optional = true, name = "pad-converse",
			description = "Pad the converse edge of the document by this many pixels.")
	public int padConverse = 5;

	@Configuration(optional = true, name = "pad-transverse",
			description = "Pad the transverse edge of the document by this many pixels.")
	public int padTransverse = 60;

	@Configuration(optional = true, description =
			"If the path to a writable document does not yet exist, a new document will be created " +
					"with this contents.")
	public List<InterfaceEvent> template = new ArrayList<>();

	@Configuration(optional = true)
	public List<Style> styles = new ArrayList<>();

	@Configuration(optional = true, name = "hover-style")
	public ObboxStyle hoverStyle = new ObboxStyle();

	@Configuration(optional = true, name = "select-style")
	public ObboxStyle selectStyle = new ObboxStyle();

	@Configuration(description = "The definitions of all distinct element types in a document.\n" +
			"A type with the id '__gap' and a single middle primitive element named 'value' must exist.  This will " +
			"be used as a placeholder when entering text before it is distinguishable as any other defined element.")
	public List<FreeNodeType> types = new ArrayList<>();

	@Configuration(optional = true, description = "The gap type is used when editing the document, for " +
			"new data whose type is not yet known.")
	public GapNodeType gap = new GapNodeType();
	@Configuration(name = "prefix-gap", optional = true, description =
			"The prefix gap type is similar to the gap type, but is used when enclosing an " +
					"existing node in a new node, where the new node visually precedes the existing node.")
	public PrefixGapNodeType prefixGap = new PrefixGapNodeType();
	@Configuration(name = "suffix-gap", optional = true, description =
			"The suffix gap type is similar to the gap type, but is used when enclosing an " +
					"existing node in a new node, where the new node visually succeeds the existing node.")
	public SuffixGapNodeType suffixGap = new SuffixGapNodeType();

	@Configuration(optional = true, description =
			"Pseudo-types representing groups of types.  Group ids can be used anywhere a type id " +
					"is required.")
	public Map<String, java.util.Set<String>> groups = new HashMap<>();

	@Configuration(optional = true, description =
			"A list of modules to activate.  Listed are modules bundled with this distribution, but " +
					"addional modules may be installed and used.")
	public List<Module> modules = new ArrayList<>();

	@Configuration(description = "The type of the root array in a document.  This is not used when " +
			"pasting code; in that case the context is used to determine the paste's potential root type.")
	public MiddleArray root;

	@Configuration(optional = true, description = "Root front-end configuration.")
	public RootFrontDataArray rootFront = new RootFrontDataArray();

	@Configuration(optional = true, name = "animate-course-placement")
	public boolean animateCoursePlacement = false;
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

	@Configuration(name = "converse-direction", optional = true,
			description = "The direction of text flow in a line.  For English, this will be RIGHT.")
	public Direction converseDirection = Direction.RIGHT;

	@Configuration(name = "transverse-direction", optional = true,
			description = "The direction of successive lines.  For English, this will be DOWN.")
	public Direction transverseDirection = Direction.DOWN;

	Grammar grammar;

	public static Reflections reflections = new Reflections("com.zarbosoft");

	public static Syntax loadSyntax(final String id, final Path path) {
		final Syntax out = LuaConf.parse(reflections, Syntax.class, path);
		out.id = id;
		out.finish();
		return out;
	}

	public void finish() {
		root.id = "value";

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

		boolean foundRoot = false;
		final Set<String> scalarTypes = new HashSet<>(); // Types that only have one back element
		final Set<String> allTypes = new HashSet<>();
		for (final FreeNodeType t : types) {
			if (t.back.isEmpty())
				throw new InvalidSyntax(String.format("Type [%s] has no back parts.", t.id));
			if (allTypes.contains(t.id))
				throw new InvalidSyntax(String.format("Multiple types with id [%s].", t.id));
			allTypes.add(t.id);
			if (t.back.size() == 1)
				scalarTypes.add(t.id);
			if (t.id.equals(root.type)) {
				foundRoot = true;
			}
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
			if (group.equals(root.type))
				foundRoot = true;
			potentiallyScalarGroups.add(group);
		}
		if (!foundRoot)
			throw new InvalidSyntax(String.format("No type or tag id matches root id [%s]", root.type));
		for (final FreeNodeType t : types) {
			if (t.back.size() == 1)
				continue;
			final Deque<Iterator<String>> stack = new ArrayDeque<>();
			stack.add(groupsThatContainType.getOrDefault(t.id, ImmutableSet.of()).iterator());
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
		for (final FreeNodeType t : types) {
			t.finish(this, allTypes, scalarTypes);
		}
		root.finish(allTypes, scalarTypes);
		rootFront.finish(root);
		gap.finish(this, allTypes, scalarTypes);
		prefixGap.finish(this, allTypes, scalarTypes);
		suffixGap.finish(this, allTypes, scalarTypes);
	}

	private Grammar getGrammar() {
		if (grammar == null) {
			grammar = new Grammar();
			types.forEach(t -> grammar.add(t.id, t.buildBackRule(this)));
			grammar.add(gap.id, gap.buildBackRule(this));
			grammar.add(prefixGap.id, prefixGap.buildBackRule(this));
			grammar.add(suffixGap.id, suffixGap.buildBackRule(this));
			groups.forEach((k, v) -> {
				final Union group = new Union();
				v.forEach(n -> group.add(new Reference(n)));
				grammar.add(k, group);
			});
			grammar.add("root", new Reference(root.type));
		}
		return grammar;
	}

	public Document create() {
		return new Document(this, new ValueArray(root,
				new Parse<Node>()
						.grammar(getGrammar())
						.parse(template.stream().map(e -> (LuxemEvent) e))
						.collect(Collectors.toList())
		));
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
		return new Document(this,
				new ValueArray(root, new Parse<Node>().grammar(getGrammar()).parse(data).collect(Collectors.toList()))
		);
	}

	public FreeNodeType getType(final String type) {
		return types.stream().filter(t -> t.id.equals(type)).findFirst().get();
	}

	public Stream<FreeNodeType> getLeafTypes(final String type) {
		if (type == null)
			return types.stream(); // Gap types
		final Set<String> group = groups.get(type);
		if (group == null)
			return Stream.of(getType(type));
		final Deque<Iterator<String>> stack = new ArrayDeque<>();
		stack.addLast(group.iterator());
		return stream(new Iterator<FreeNodeType>() {
			@Override
			public boolean hasNext() {
				return !stack.isEmpty();
			}

			@Override
			public FreeNodeType next() {
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
