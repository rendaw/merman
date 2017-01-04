package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zarbosoft.bonestruct.editor.model.Document;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.animation.Interpolator;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;

public class Context {
	public Grammar hotkeyGrammar;
	public EventStream<Action> hotkeyParse;
	public String hotkeySequence = "";
	public WeakHashMap<Set<VisualNode.Tag>, WeakReference<Style.Baked>> styleCache = new WeakHashMap<>();
	public WeakHashMap<Set<VisualNode.Tag>, WeakReference<Hotkeys>> hotkeysCache = new WeakHashMap<>();
	public final Wall wall;
	public Group background;

	public void root(final VisualNodePart node) {
		wall.clear(this);
		final Course course = new Course(this);
		wall.add(this, 0, ImmutableList.of(course));
		node.rootAlignments(this, ImmutableMap.of());
		final Brick first = node.createFirstBrick(this);
		course.add(this, 0, ImmutableList.of(first));
		node.select(this);
		this.fillFromEndBrick(first);
	}

	public void fillFromEndBrick(final Brick end) {
		if (idleFill == null) {
			idleFill = new IdleFill();
			addIdle(idleFill);
		}
		idleFill.ends.addLast(end);
	}

	public class IdleFill extends IdleTask {
		public Deque<Brick> ends = new ArrayDeque<>();

		@Override
		protected int priority() {
			return 100;
		}

		@Override
		public void run() {
			if (ends.isEmpty()) {
				idleFill = null;
				return;
			}
			final Brick next = ends.pollLast();
			final Brick created = next.createNext(Context.this);
			if (created != null) {
				next.addAfter(Context.this, created);
				ends.addLast(created);
			}
			Context.this.addIdle(this);
		}
	}

	IdleFill idleFill = null;

	public void setSelection(final Context context, final Selection selection) {
		if (this.selection != null) {
			this.selection.clear(context);
		}
		this.selection = selection;
		hotkeyGrammar = new Grammar();
		final Union union = new Union();
		for (final Action action : selection.getActions(context)) {
			Node rule = action.buildRule();
			if (rule == null)
				rule = new Sequence()
						.add(new Terminal(new Keyboard.Event(Keyboard.Key.PAGE_UP, false, false, false)))
						.add(Keyboard.ruleFromString(String.format(":%s", action.getName())));
			union.add(new BakedOperator(rule, store -> store.pushStack(action)));
		}
		hotkeyGrammar.add("root", union);
	}

	public Style.Baked getStyle(final Set<VisualNode.Tag> tags) {
		final Optional<Style.Baked> found = styleCache
				.entrySet()
				.stream()
				.filter(e -> tags.equals(e.getKey()))
				.map(e -> e.getValue().get())
				.findFirst();
		if (found.isPresent())
			return found.get();
		final Style.Baked out = new Style.Baked(tags);
		for (final Style style : syntax.styles) {
			if (tags.containsAll(style.tags)) {
				out.merge(style);
			}
		}
		styleCache.put(out.tags, new WeakReference<>(out));
		return out;
	}

	public Hotkeys getHotkeys(final Set<VisualNode.Tag> tags) {
		final Optional<Hotkeys> found = hotkeysCache
				.entrySet()
				.stream()
				.filter(e -> tags.equals(e.getKey()))
				.map(e -> e.getValue().get())
				.findFirst();
		if (found.isPresent())
			return found.get();
		final Hotkeys out = new Hotkeys(tags);
		for (final Hotkeys hotkeys : syntax.hotkeys) {
			if (tags.containsAll(hotkeys.tags)) {
				out.merge(hotkeys);
			}
			hotkeysCache.put(out.tags, new WeakReference<>(out));
		}
		return out;
	}

	public static abstract class Action {
		public abstract Node buildRule();

		public abstract void run(Context context);

		public abstract String getName();
	}

	public static abstract class Selection {

		public abstract void clear(Context context);

		public void receiveText(final Context context, final String text) {
		}

		public abstract Iterable<Action> getActions(Context context);
	}

	public static abstract class Hoverable {
		public abstract void clear(Context context);
	}

	public class HoverIdle extends IdleTask {
		public Vector point = null;
		Context context;
		Brick at;

		public HoverIdle(final Context context) {
			this.context = context;
			at = hoverBrick == null ? (
					context.wall.children.get(0).children.isEmpty() ?
							null :
							context.wall.children.get(0).children.get(0)
			) : hoverBrick;
		}

		@Override
		public void run() {
			// TODO store indexes rather than brick ref
			if (at == null) {
				hoverIdle = null;
				return;
			}
			if (point == null) {
				if (hover != null)
					hover.clear(context);
				hover = null;
				hoverBrick = null;
				hoverIdle = null;
				return;
			}
			if (point.transverse < at.parent.transverseStart && at.parent.index > 0) {
				at = context.wall.children.get(at.parent.index - 1).children.get(0);
			} else if (point.transverse > at.parent.transverseEdge(context) &&
					at.parent.index < wall.children.size() - 1) {
				at = context.wall.children.get(at.parent.index + 1).children.get(0);
			} else {
				while (point.converse < at.getConverse(context) && at.index > 0) {
					at = at.parent.children.get(at.index - 1);
				}
				while (point.converse >= at.converseEdge(context) && at.index < at.parent.children.size() - 1) {
					at = at.parent.children.get(at.index + 1);
				}
				final Hoverable old = hover;
				hover = at.getVisual().hover(context);
				if (hover != old) {
					if (old != null)
						old.clear(context);
				}
				hoverBrick = at;
				hoverIdle = null;
				return;
			}
			addIdle(this);
		}
	}

	public final Syntax syntax;
	public final Document document;
	public int edge = 0;
	public int transverseEdge = 0;
	public Brick hoverBrick;
	public Hoverable hover;
	public HoverIdle hoverIdle;
	public Selection selection;

	public Context(
			final Syntax syntax, final Document document, final Consumer<IdleTask> addIdle, final Wall wall
	) {
		this.syntax = syntax;
		this.document = document;
		this.addIdle = addIdle;
		this.wall = wall;
	}

	private final Consumer<IdleTask> addIdle;

	public void addIdle(final IdleTask task) {
		this.addIdle.accept(task);
	}

	static class TheInterpolator extends Interpolator {
		@Override
		protected double curve(double t) {
			t = t * 2;
			if (t * 2 < 1)
				return Math.pow(t, 3) / 2;
			else
				return Math.pow(t - 1, 3) / 2 + 1;
		}
	}

	TheInterpolator interpolator = new TheInterpolator();

	public Vector sceneToVector(final Pane scene, final double x, final double y) {
		int converse = 0;
		int transverse = 0;
		switch (syntax.converseDirection) {
			case UP:
				converse = (int) (scene.heightProperty().doubleValue() - y);
				break;
			case DOWN:
				converse = (int) y;
				break;
			case LEFT:
				converse = (int) (scene.widthProperty().doubleValue() - x);
				break;
			case RIGHT:
				converse = (int) x;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				transverse = (int) (scene.heightProperty().doubleValue() - x);
				break;
			case DOWN:
				transverse = (int) y;
				break;
			case LEFT:
				transverse = (int) (scene.widthProperty().doubleValue() - x);
				break;
			case RIGHT:
				transverse = (int) x;
				break;
		}
		return new Vector(converse, transverse);
	}

	public Point2D toScreen(final Vector source) {
		double x = 0, y = 0;
		switch (syntax.converseDirection) {
			case UP:
				y = edge - source.converse;
				break;
			case DOWN:
				y = source.converse;
				break;
			case LEFT:
				x = edge - source.converse;
				break;
			case RIGHT:
				x = source.converse;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				y = transverseEdge - source.transverse;
				break;
			case DOWN:
				y = source.transverse;
				break;
			case LEFT:
				x = transverseEdge - source.transverse;
				break;
			case RIGHT:
				x = source.transverse;
				break;
		}
		return new Point2D(x, y);
	}

	public Point2D toScreenSpan(final Vector source) {
		double x = 0, y = 0;
		switch (syntax.converseDirection) {
			case UP:
			case DOWN:
				x = source.transverse;
				y = source.converse;
				break;
			case LEFT:
			case RIGHT:
				x = source.converse;
				y = source.transverse;
				break;
		}
		return new Point2D(x, y);
	}

	public void translate(final javafx.scene.Node node, final Vector vector) {
		int x = 0;
		int y = 0;
		switch (syntax.converseDirection) {
			case UP:
				y = (int) node.getLayoutBounds().getHeight() + vector.converse;
				break;
			case DOWN:
				y = vector.converse;
				break;
			case LEFT:
				x = (int) node.getLayoutBounds().getWidth() + vector.converse;
				break;
			case RIGHT:
				x = vector.converse;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				y = (int) node.getLayoutBounds().getHeight() + vector.transverse;
				break;
			case DOWN:
				y = vector.transverse;
				break;
			case LEFT:
				x = (int) node.getLayoutBounds().getWidth() + vector.transverse;
				break;
			case RIGHT:
				x = vector.transverse;
				break;
		}
		/*
		final TranslateTransition translation = new TranslateTransition(Duration.seconds(1), node);
		translation.setInterpolator(interpolator);
		translation.setToX(x);
		translation.setToY(y);
		translation.play();
		*/
		node.setLayoutX(x);
		node.setLayoutY(y);
	}
}
