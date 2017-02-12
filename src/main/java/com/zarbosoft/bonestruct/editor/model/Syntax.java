package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.LuxemEvent;
import com.zarbosoft.luxemj.path.LuxemArrayPath;
import com.zarbosoft.luxemj.path.LuxemPath;
import com.zarbosoft.pidgoon.events.*;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Mutable;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Luxem.Configuration
public class Syntax {

	@Luxem.Configuration(description = "The name of the syntax.  This may be used in menus and dialogs.")
	public String name;

	@Luxem.Configuration(optional = true, description = "The background color of the document.")
	public Color background = Color.WHITE;

	@Luxem.Configuration(optional = true, name = "pad-converse",
			description = "Pad the converse edge of the document by this many pixels.")
	public int padConverse = 5;

	@Luxem.Configuration(optional = true, name = "pad-transverse",
			description = "Pad the transverse edge of the document by this many pixels.")
	public int padTransverse = 60;

	@Luxem.Configuration(optional = true, description =
			"If the path to a writable document does not yet exist, a new document will be created " +
					"with this contents.")
	public List<LuxemEvent> template;

	@Luxem.Configuration(optional = true)
	public List<Style> styles;

	@Luxem.Configuration(optional = true, name = "hover-style")
	public ObboxStyle hoverStyle = new ObboxStyle();

	@Luxem.Configuration(optional = true, name = "select-style")
	public ObboxStyle selectStyle = new ObboxStyle();

	@Luxem.Configuration(description = "The definitions of all distinct element types in a document.\n" +
			"A type with the id '__gap' and a single middle primitive element named 'value' must exist.  This will " +
			"be used as a placeholder when entering text before it is distinguishable as any other defined element.")
	public List<FreeNodeType> types;

	@Luxem.Configuration(optional = true, description = "The gap type is used when editing the document, for " +
			"new data whose type is not yet known.")
	public GapNodeType gap = new GapNodeType();
	@Luxem.Configuration(name = "prefix-gap", optional = true, description =
			"The prefix gap type is similar to the gap type, but is used when enclosing an " +
					"existing node in a new node, where the new node visually precedes the existing node.")
	public PrefixGapNodeType prefixGap = new PrefixGapNodeType();
	@Luxem.Configuration(name = "suffix-gap", optional = true, description =
			"The suffix gap type is similar to the gap type, but is used when enclosing an " +
					"existing node in a new node, where the new node visually succeeds the existing node.")
	public SuffixGapNodeType suffixGap = new SuffixGapNodeType();

	@Luxem.Configuration(optional = true, description =
			"Pseudo-types representing groups of types.  Group ids can be used anywhere a type id " +
					"is required.")
	public Map<String, java.util.Set<String>> groups;

	@Luxem.Configuration(optional = true, description =
			"A list of plugins to activate.  Listed are plugins bundled with this distribution, but " +
					"addional plugins may be installed and used.")
	public List<Plugin> plugins;

	@Luxem.Configuration(description = "The id of the type of root elements in a document.  This is not used when " +
			"pasting code; in that case the context is used to determine the paste's potential root type.")
	public String root;

	@Luxem.Configuration(name = "root-prefix", optional = true)
	public List<FrontConstantPart> rootPrefix;
	@Luxem.Configuration(name = "root-separator", optional = true)
	public List<FrontConstantPart> rootSeparator = new ArrayList<>();
	@Luxem.Configuration(name = "root-suffix", optional = true)
	public List<FrontConstantPart> rootSuffix;

	@Luxem.Configuration(optional = true, name = "root-hotkeys")
	public Map<String, com.zarbosoft.luxemj.grammar.Node> rootHotkeys = new HashMap<>();

	@Luxem.Configuration(optional = true)
	public List<Hotkeys> hotkeys;

	@Luxem.Configuration(optional = true, name = "modal-primitive-editing", description =
			"In modeless editing, a selected primitive is always in direct editing mode.  Non-hotkey keypresses " +
					"will modify the primitive text.  In modal editing an extra 'enter' action will be available " +
					"to enter the direct editing mode.  After entering, non-hotkey kepresses will modify the " +
					"primitive text.  In the indirect editing action names will more closely match the action names " +
					"of other node types.")
	public boolean modalPrimitiveEditing = false;

	@Luxem.Configuration(optional = true, name = "animate-course-placement")
	public boolean animateCoursePlacement = false;
	public String id; // Fake final - don't modify (set in loadSyntax)

	@Luxem.Configuration
	public enum Direction {
		@Luxem.Configuration(name = "up")
		UP,
		@Luxem.Configuration(name = "down")
		DOWN,
		@Luxem.Configuration(name = "left")
		LEFT,
		@Luxem.Configuration(name = "right")
		RIGHT;
		// TODO boustrophedon

		public double extract(final Bounds bounds) {
			switch (this) {
				case UP:
				case DOWN:
					return bounds.getHeight();
				case LEFT:
				case RIGHT:
					return bounds.getWidth();
			}
			return 0; // unreachable
		}

		public Point2D consVector(final double value) {
			switch (this) {
				case UP:
					return new Point2D(0, -value);
				case DOWN:
					return new Point2D(0, value);
				case LEFT:
					return new Point2D(-value, 0);
				case RIGHT:
					return new Point2D(value, 0);
			}
			return null; // unreachable
		}
	}

	@Luxem.Configuration(name = "converse-direction", optional = true,
			description = "The direction of text flow in a line.  For English, this will be RIGHT.")
	public Direction converseDirection = Direction.RIGHT;

	@Luxem.Configuration(name = "transverse-direction", optional = true,
			description = "The direction of successive lines.  For English, this will be DOWN.")
	public Direction transverseDirection = Direction.DOWN;

	Grammar grammar;

	public static Syntax loadSyntax(final String id, final InputStream stream) {
		final Syntax out = new com.zarbosoft.luxemj.Parse<Syntax>()
				.grammar(com.zarbosoft.bonestruct.editor.luxem.Luxem.grammarForType(Syntax.class))
				.errorHistory(5)
				.dumpAmbiguity(true)
				.uncertainty(100)
				.eventUncertainty(256)
				.node("root")
				.parse(stream);
		out.id = id;

		// Check data

		// jfx, qt, and swing don't support vertical languages
		if (!ImmutableSet.of(Direction.LEFT, Direction.RIGHT).contains(out.converseDirection) ||
				(out.transverseDirection != Direction.DOWN))
			throw new InvalidSyntax("Currently only converse directions left/right and transverse down are supported.");
		switch (out.converseDirection) {
			case LEFT:
			case RIGHT:
				switch (out.transverseDirection) {
					case LEFT:
					case RIGHT:
						throw new InvalidSyntax("Secondary direction must cross converse direction axis.");
				}
				break;
			case UP:
			case DOWN:
				switch (out.transverseDirection) {
					case UP:
					case DOWN:
						throw new InvalidSyntax("Secondary direction must cross converse direction axis.");
				}
				break;
		}

		{
			final Deque<Pair<PSet<String>, Iterator<String>>> stack = new ArrayDeque<>();
			stack.addLast(new Pair<>(HashTreePSet.empty(), out.groups.keySet().iterator()));
			while (!stack.isEmpty()) {
				final Pair<PSet<String>, Iterator<String>> top = stack.pollLast();
				if (!top.second.hasNext())
					continue;
				final String childKey = top.second.next();
				final Set<String> child = out.groups.get(childKey);
				if (child == null)
					continue;
				if (top.first.contains(childKey))
					throw new InvalidSyntax(String.format("Circular reference in group [%s].", childKey));
				stack.addLast(top);
				stack.addLast(new Pair<>(top.first.plus(childKey), child.iterator()));
			}
		}

		boolean foundRoot = false;
		final Set<String> allTypeIds = new HashSet<>();
		for (final FreeNodeType t : out.types) {
			if (t.back.isEmpty())
				throw new InvalidSyntax(String.format("Type [%s] has no back parts.", t.id));
			if (allTypeIds.contains(t.id))
				throw new InvalidSyntax(String.format("Multiple types with id [%s].", t.id));
			allTypeIds.add(t.id);
			if (t.id.equals(out.root)) {
				foundRoot = true;
			}
		}
		if (out.gap == null)
			throw new InvalidSyntax("Gap definition missing.");
		if (out.prefixGap == null)
			throw new InvalidSyntax("Prefix gap definition missing.");
		if (out.suffixGap == null)
			throw new InvalidSyntax("Suffix gap definition missing.");
		for (final Map.Entry<String, Set<String>> pair : out.groups.entrySet()) {
			final String group = pair.getKey();
			for (final String child : pair.getValue())
				if (!allTypeIds.contains(child) && !out.groups.containsKey(child))
					throw new InvalidSyntax(String.format("Group [%s] refers to non-existant member [%s].",
							group,
							child
					));
			if (allTypeIds.contains(group))
				throw new InvalidSyntax(String.format("Group id [%s] already used.", group));
			allTypeIds.add(group);
			if (group.equals(out.root))
				foundRoot = true;
		}
		if (!foundRoot)
			throw new InvalidSyntax(String.format("No type or tag id matches root id [%s]", out.root));
		for (final FreeNodeType t : out.types) {
			t.finish(out);
		}
		out.gap.finish(out);
		out.prefixGap.finish(out);
		out.suffixGap.finish(out);
		return out;
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
			grammar.add("root", new BakedOperator(new Repeat(new BakedOperator(new Reference(root), store -> {
				return Helper.stackSingleElement(store);
			})), store -> {
				final List<DataNode.Value> out = new ArrayList<>();
				store = (Store) Helper.<DataNode.Value>stackPopSingleList(store, out::add);
				return store.pushStack(out);
			}));
		}
		return grammar;
	}

	public Document create() {
		final EventStream<List<Node>> stream =
				new Parse<List<Node>>().stack(() -> 0).grammar(getGrammar()).node("root").parse();
		final Mutable<LuxemPath> path = new Mutable<>(new LuxemArrayPath(null));
		template.forEach(e -> {
			path.value = path.value.push(e);
			stream.push(e, path.value.toString());
		});
		return new Document(this, new DataArray.Value(null, stream.finish()));
	}

	public Document load(final File file) throws FileNotFoundException, IOException {
		try (
				FileInputStream data = new FileInputStream(file)
		) {
			return load(data);
		}
	}

	public Document load(final String string) {
		return load(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	public Document load(final InputStream data) {
		return new Document(this, new DataArray.Value(null,
				new com.zarbosoft.luxemj.Parse<List<Node>>()
						.stack(() -> 0)
						.grammar(getGrammar())
						.node("root")
						.parse(data)
		));
	}

	public FreeNodeType getType(final String type) {
		return types.stream().filter(t -> t.id.equals(type)).findFirst().get();
	}

	public Set<FreeNodeType> getLeafTypes(final String type) {
		final Set<String> group = groups.get(type);
		if (group == null)
			return ImmutableSet.of(getType(type));
		final Set<FreeNodeType> out = new HashSet<>();
		final Deque<Iterator<String>> stack = new ArrayDeque<>();
		stack.addLast(groups.keySet().iterator());
		while (!stack.isEmpty()) {
			final Iterator<String> top = stack.pollLast();
			if (!top.hasNext())
				continue;
			final String childKey = top.next();
			final Set<String> child = groups.get(childKey);
			if (child == null) {
				out.add(getType(childKey));
			} else {
				stack.addLast(top);
				stack.addLast(child.iterator());
			}
		}
		return out;
	}

}
