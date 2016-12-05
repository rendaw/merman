package com.zarbosoft.bonestruct.model;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.bonestruct.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.visual.Hotkeys;
import com.zarbosoft.bonestruct.visual.Obbox;
import com.zarbosoft.bonestruct.visual.Style;
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
import javafx.collections.FXCollections;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Luxem.Configuration
public class Syntax {

	@Luxem.Configuration
	public String name;

	@Luxem.Configuration(optional = true)
	public Color background = Color.WHITE;

	@Luxem.Configuration(optional = true, name = "pad-converse")
	public int padConverse = 5;

	@Luxem.Configuration(optional = true, name = "pad-transverse")
	public int padTransverse = 5;

	@Luxem.Configuration
	public List<LuxemEvent> template;

	@Luxem.Configuration
	public List<Style> styles;

	@Luxem.Configuration
	public List<NodeType> types;

	@Luxem.Configuration
	public Map<String, java.util.Set<String>> groups;

	@Luxem.Configuration
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
	public Map<String, com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node> rootHotkeys = new HashMap<>();

	@Luxem.Configuration(optional = true)
	public Obbox.Settings select = new Obbox.Settings();

	@Luxem.Configuration(optional = true)
	public List<Hotkeys> hotkeys;

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

	@Luxem.Configuration(name = "converse-direction", optional = true)
	public Direction converseDirection = Direction.RIGHT;

	@Luxem.Configuration(name = "transverse-direction", optional = true)
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
				.grammar(com.zarbosoft.bonestruct.Luxem.grammarForType(Syntax.class))
				.errorHistory(5)
				.uncertainty(100)
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
			if (t.id.equals(out.root))
				foundRoot = true;
		}
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
			types.forEach(t -> grammar.add(t.id, t.buildLoadRule()));
			groups.forEach((k, v) -> {
				final Union group = new Union();
				v.forEach(n -> group.add(new Reference(n)));
				grammar.add(k, group);
			});
			grammar.add("root", new BakedOperator(new Repeat(new BakedOperator(new Reference(root), store -> {
				return Helper.stackSingleElement(store);
			})), store -> {
				final List<Node> out = new ArrayList<>();
				store = (Store) Helper.<Node>stackPopSingleList(store, node -> {
					out.add(node);
				});
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
		return new Document(this, FXCollections.observableList(stream.finish()));
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
		return new Document(
				this,
				FXCollections.observableList(new com.zarbosoft.luxemj.Parse<List<Node>>()
						.stack(() -> 0)
						.grammar(getGrammar())
						.node("root")
						.parse(data))
		);
	}
}
