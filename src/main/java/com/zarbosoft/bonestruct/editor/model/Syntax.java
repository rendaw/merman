package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
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
			description = "Pad the converse edge of the editor by this many pixels.")
	public int padConverse = 5;

	@Luxem.Configuration(optional = true, name = "pad-transverse",
			description = "Pad the transverse edge of the editor by this many pixels.")
	public int padTransverse = 5;

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
			"A type with the id __bud, and a single middle primitive element named 'value' must exist.  This will be " +
			"used as a placeholder when entering text before it is distinguishable as any other defined element.")
	public List<NodeType> types;

	@Luxem.Configuration(optional = true, description =
			"Pseudo-types representing a group of types.  Group ids can be used anywhere a type id " +
					"is required.")
	public Map<String, java.util.Set<String>> groups;

	@Luxem.Configuration(description = "The id of the type of root elements in a document.  This is not used when " +
			"pasting code; in that case the context is used to determine the paste's root.")
	public String root;
	// Root applies to whole document parsing
	// For parsing text pastes, use the current location to find the correct rule

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
	public NodeType bud;

	@Luxem.Configuration(optional = true, name = "modal-primitive-editing", description =
			"In modeless editing, a selected primitive is always in direct editing mode.  Non-hotkey keypresses " +
					"will modify the primitive text.  In modal editing an extra 'enter' action will be available " +
					"to enter the direct editing mode.  After entering, non-hotkey kepresses will modify the " +
					"primitive text.  In the indirect editing action names will more closely match the action names " +
					"of other node types.")
	public boolean modalPrimitiveEditing = false;

	@Luxem.Configuration
	public enum Direction {
		@Luxem.Configuration(name = "up")UP, @Luxem.Configuration(name = "down")
		DOWN, @Luxem.Configuration(name = "left")
		LEFT, @Luxem.Configuration(name = "right")
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
			description = "The direction of text flow in a line.  In English, this will be RIGHT.")
	public Direction converseDirection = Direction.RIGHT;

	@Luxem.Configuration(name = "transverse-direction", optional = true,
			description = "The direction of successive lines.  In English, this will be DOWN.")
	public Direction transverseDirection = Direction.DOWN;

	@Luxem.Configuration
	public enum CompactionMode {
		@Luxem.Configuration(name = "bottom-up")
		BOTTOM_UP,
		/*
		@Luxem.Configuration(name = "greatest-gain")
		GREATEST_GAIN,
		@Luxem.Configuration(name = "priority")
		PRIORITY,
		*/
	}

	@Luxem.Configuration(name = "compaction-mode", optional = true)
	public CompactionMode compactionMode = CompactionMode.BOTTOM_UP;

	Grammar grammar;

	public static Syntax loadSyntax(final InputStream stream) {
		final Syntax out = new com.zarbosoft.luxemj.Parse<Syntax>()
				.grammar(com.zarbosoft.bonestruct.editor.luxem.Luxem.grammarForType(Syntax.class))
				.errorHistory(5)
				.dumpAmbiguity(true)
				.uncertainty(100)
				.eventUncertainty(256)
				.node("root")
				.parse(stream);
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
		boolean foundRoot = false;
		final Set<String> singleNodes = new HashSet<>();
		final Set<String> arrayNodes = new HashSet<>();
		final Map<String, Pair<Boolean, Set<String>>> reverseGroups = new HashMap<>();
		for (final Map.Entry<String, Set<String>> pair : out.groups.entrySet()) {
			final String group = pair.getKey();
			final Set<String> children = pair.getValue();
			{
				final Pair<Boolean, Set<String>> set =
						reverseGroups.getOrDefault(group, new Pair<>(true, new HashSet<>()));
				reverseGroups.put(group, set);
			}
			children.forEach(child -> {
				final Pair<Boolean, Set<String>> set =
						reverseGroups.getOrDefault(child, new Pair<>(true, new HashSet<>()));
				set.second.add(group);
				reverseGroups.put(child, set);
			});
		}
		for (final NodeType t : out.types) {
			if (t.back.isEmpty())
				throw new InvalidSyntax(String.format("Type [%s] has no back parts.", t.id));
			if (arrayNodes.contains(t.id))
				throw new InvalidSyntax(String.format("Multiple types with id [%s].", t.id));
			arrayNodes.add(t.id);
			if (t.back.size() == 1)
				singleNodes.add(t.id);
			if (reverseGroups.containsKey(t.id) && t.back.size() > 1)
				reverseGroups.get(t.id).first = false;
			if (t.id.equals("__bud")) {
				if (out.bud != null)
					throw new InvalidSyntax("Multiple definitions of [__bud].");
				if (t.middle.size() != 1)
					throw new InvalidSyntax("__bud must have one middle element.");
				if (!t.middle.containsKey("value"))
					throw new InvalidSyntax("__bud must have one middle element named [value].");
				if (!(t.middle.get("value") instanceof DataPrimitive))
					throw new InvalidSyntax("__bud middle element [value] must be primitive.");
				out.bud = t;
			}
			if (t.id.equals(out.root)) {
				foundRoot = true;
			}
		}
		if (out.bud == null)
			throw new InvalidSyntax("__bud type definition missing.");
		boolean changing = true;
		while (changing) {
			changing = false;
			for (final Map.Entry<String, Pair<Boolean, Set<String>>> pair : reverseGroups.entrySet())
				if (!pair.getValue().first)
					for (final String next : pair.getValue().second)
						if (reverseGroups.containsKey(next)) {
							final Pair<Boolean, Set<String>> reverseGroup = reverseGroups.get(next);
							if (reverseGroup.first) {
								reverseGroups.get(next).first = false;
								changing = true;
							}
						}
		}
		for (final Map.Entry<String, Set<String>> pair : out.groups.entrySet()) {
			final String group = pair.getKey();
			final Set<String> children = pair.getValue();
			if (arrayNodes.contains(group))
				throw new InvalidSyntax(String.format("Group id [%s] already used.", group));
			arrayNodes.add(group);
			if (reverseGroups.containsKey(group) && reverseGroups.get(group).first)
				singleNodes.add(group);
			if (group.equals(out.root))
				foundRoot = true;
		}
		if (!foundRoot)
			throw new InvalidSyntax(String.format("No type or tag id matches root id [%s]", out.root));
		for (final NodeType t : out.types) {
			t.finish(singleNodes, arrayNodes);
		}
		return out;
	}

	private Grammar getGrammar() {
		if (grammar == null) {
			grammar = new Grammar();
			types.forEach(t -> grammar.add(t.id, t.buildLoadRule(this)));
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
		final EventStream<List<DataNode.Value>> stream =
				new Parse<List<DataNode.Value>>().stack(() -> 0).grammar(getGrammar()).node("root").parse();
		final Mutable<LuxemPath> path = new Mutable<>(new LuxemArrayPath(null));
		template.forEach(e -> {
			path.value = path.value.push(e);
			stream.push(e, path.value.toString());
		});
		return new Document(this, new DataArray.Value(this, stream.finish()));
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
		return new Document(this, new DataArray.Value(
				this,
				new com.zarbosoft.luxemj.Parse<List<DataNode.Value>>()
						.stack(() -> 0)
						.grammar(getGrammar())
						.node("root")
						.parse(data)
		));
	}
}
