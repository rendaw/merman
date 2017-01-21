package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.ChainComparator;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.model.*;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.raw.RawText;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Bedding;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.bonestruct.editor.visual.wall.Wall;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.layout.Pane;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class Context {
	public final History history;
	public Grammar hotkeyGrammar;
	public EventStream<Action> hotkeyParse;
	public String hotkeySequence = "";
	public WeakHashMap<Set<VisualNode.Tag>, WeakReference<Style.Baked>> styleCache = new WeakHashMap<>();
	public WeakHashMap<Set<VisualNode.Tag>, WeakReference<Hotkeys>> hotkeysCache = new WeakHashMap<>();
	public final Wall wall;
	public Group background;
	private final Iterable<Action> globalActions;
	public VisualNodePart window;
	public Brick cornerstone;
	public int cornerstoneTransverse;
	public int scrollTransverse;
	public Banner banner;
	public Details details;
	private final Set<SelectionListener> selectionListeners = new HashSet<>();
	private final Set<HoverListener> hoverListeners = new HashSet<>();
	public final VisualAttachmentAdapter selectionAdapter = new VisualAttachmentAdapter();
	public final TransverseExtentsAdapter selectionExtentsAdapter = new TransverseExtentsAdapter();
	public List<Plugin.State> plugins;

	public void addSelectionBrickListener(final VisualAttachmentAdapter.BoundsListener listener) {
		selectionAdapter.addListener(this, listener);
	}

	public static class Banner {
		private RawText text;
		private final PriorityQueue<BannerMessage> queue =
				new PriorityQueue<>(11, new ChainComparator<BannerMessage>().greaterFirst(m -> m.priority).build());
		private BannerMessage current;
		private final Timer timer = new Timer();
		private Brick brick;
		private int targetTransverse;
		private int targetBedding;
		private Bedding bedding;

		public Banner(final Context context) {
			context.selectionExtentsAdapter.addListener(context, new TransverseExtentsAdapter.Listener() {
				int brickBedding;

				@Override
				public void transverseChanged(final Context context, final int transverse) {
					Banner.this.targetTransverse = transverse;
					placeText(context);
				}

				private void placeText(final Context context) {
					if (text == null)
						return;
					text.setTransverse(targetTransverse + targetBedding - text.transverseSpan(),
							context.transverseEdge,
							context.syntax.animateCoursePlacement
					);
				}

				@Override
				public void transverseEdgeChanged(final Context context, final int transverse) {

				}

				@Override
				public void beddingAfterChanged(final Context context, final int beddingAfter) {

				}

				@Override
				public void beddingBeforeChanged(final Context context, final int beddingBefore) {
					targetBedding = beddingBefore;
					placeText(context);
				}
			});
			context.selectionAdapter.addListener(context, new VisualAttachmentAdapter.BoundsListener() {
				@Override
				public void firstChanged(final Context context, final Brick first) {
					if (brick != null && bedding != null)
						brick.removeBedding(context, bedding);
					brick = first;
					if (bedding != null) {
						brick.addBedding(context, bedding);
					}
				}

				@Override
				public void lastChanged(final Context context, final Brick last) {

				}
			});
		}

		public void addMessage(final Context context, final BannerMessage message) {
			if (queue.isEmpty()) {
				text = RawText.create(context, context.getStyle(ImmutableSet.of(new VisualNode.PartTag("banner"))));
				context.background.getChildren().add(text.getVisual());
				text.setTransverse(targetTransverse + targetBedding - text.transverseSpan(),
						context.transverseEdge,
						false
				);
				bedding = new Bedding(text.transverseSpan(), 0);
				brick.addBedding(context, bedding);
			}
			queue.add(message);
			update(context);
		}

		private void update(final Context context) {
			if (queue.isEmpty()) {
				if (text != null) {
					context.background.getChildren().remove(text.getVisual());
					text = null;
					brick.removeBedding(context, bedding);
					bedding = null;
				}
			} else if (queue.peek() != current) {
				current = queue.peek();
				text.setText(current.text);
				timer.purge();
				if (current.duration != null)
					try {
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								context.addIdle(new IdleTask() {
									@Override
									protected void runImplementation() {
										queue.poll();
										update(context);
									}

									@Override
									protected void destroyed() {

									}
								});
							}
						}, current.duration.toMillis());
					} catch (final IllegalStateException e) {
						// While shutting down
					}
			}
		}

		public void destroy(final Context context) {
			timer.cancel();
		}

		public void removeMessage(final Context context, final BannerMessage message) {
			if (queue.isEmpty())
				return; // TODO implement message destroy cb, extraneous removeMessages unnecessary
			queue.remove(message);
			if (queue.isEmpty())
				timer.purge();
			update(context);
		}
	}

	public static class Details {

	}

	public abstract static class SelectionListener {

		public abstract void selectionChanged(Context context, Selection selection);
	}

	public abstract static class HoverListener {

		public abstract void hoverChanged(Context context, Hoverable selection);
	}

	public void addSelectionListener(final SelectionListener listener) {
		this.selectionListeners.add(listener);
	}

	public void removeSelectionListener(final SelectionListener listener) {
		this.selectionListeners.remove(listener);
	}

	public void addHoverListener(final HoverListener listener) {
		this.hoverListeners.add(listener);
	}

	public void removeHoverListener(final HoverListener listener) {
		this.hoverListeners.remove(listener);
	}

	public void fillFromEndBrick(final Brick end) {
		if (idleFill == null) {
			idleFill = new IdleFill();
			addIdle(idleFill);
		}
		idleFill.ends.addLast(end);
	}

	private void fillFromStartBrick(final Brick start) {
		if (idleFill == null) {
			idleFill = new IdleFill();
			addIdle(idleFill);
		}
		idleFill.starts.addLast(start);
	}

	public void clearHover() {
		hover.clear(this);
		hover = null;
	}

	public class IdleFill extends IdleTask {
		public Deque<Brick> ends = new ArrayDeque<>();
		public Deque<Brick> starts = new ArrayDeque<>();

		@Override
		protected int priority() {
			return 100;
		}

		@Override
		public void runImplementation() {
			if (ends.isEmpty() && starts.isEmpty()) {
				idleFill = null;
				return;
			}
			if (!ends.isEmpty()) {
				final Brick next = ends.pollLast();
				final Brick created = next.createNext(Context.this);
				if (created != null) {
					next.addAfter(Context.this, created);
					ends.addLast(created);
				}
			}
			if (!starts.isEmpty()) {
				final Brick previous = starts.pollLast();
				final Brick created = previous.createPrevious(Context.this);
				if (created != null) {
					previous.addBefore(Context.this, created);
					starts.addLast(created);
				}
			}
			Context.this.addIdle(this);
		}

		@Override
		protected void destroyed() {
			idleFill = null;
		}
	}

	public IdleFill idleFill = null;

	public IdleTask idleClick = null;

	public void setSelection(final Selection selection) {
		if (this.selection != null) {
			this.selection.clear(this);
		}
		this.selection = selection;

		final VisualNodePart visual = this.selection.getVisual();
		if (!visual.isAncestor(window)) {
			window = visual;
			window.rootAlignments(this, ImmutableMap.of());
			// TODO set depth indicator
		}
		final Brick newCornerstone = visual.getFirstBrick(this);
		if (newCornerstone == null) {
			cornerstone = visual.createFirstBrick(this);
			cornerstoneTransverse = 0;
		} else {
			cornerstone = newCornerstone;
			cornerstoneTransverse = newCornerstone.parent.transverseStart;
		}

		ImmutableSet.copyOf(selectionListeners).forEach(l -> l.selectionChanged(this, selection));
		selectionAdapter.setBase(this, visual);
		selectionAdapter.addListener(this, selectionExtentsAdapter.boundsListener);

		fillFromEndBrick(cornerstone);
		fillFromStartBrick(cornerstone);

		hotkeyGrammar = new Grammar();
		final Union union = new Union();
		for (final Action action : Iterables.concat(selection.getActions(this), globalActions)) {
			final List<com.zarbosoft.luxemj.grammar.Node> hotkeyEntries =
					selection.getHotkeys(this).hotkeys.get(action.getName());
			if (hotkeyEntries == null)
				continue;
			for (final com.zarbosoft.luxemj.grammar.Node hotkey : hotkeyEntries) {
				union.add(new BakedOperator(hotkey.build(), store -> store.pushStack(action)));
			}
		}
		hotkeyGrammar.add("root", union);
	}

	public Style.Baked getStyle(final Set<VisualNode.Tag> tags) {
		final Optional<Style.Baked> found = styleCache
				.entrySet()
				.stream()
				.filter(e -> tags.equals(e.getKey()))
				.map(e -> e.getValue().get())
				.filter(v -> v != null)
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
				.filter(v -> v != null)
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
		public abstract void run(Context context);

		public abstract String getName();
	}

	public static abstract class Selection {
		protected abstract void clear(Context context);

		protected abstract Hotkeys getHotkeys(Context context);

		public void receiveText(final Context context, final String text) {
		}

		public abstract Iterable<Action> getActions(Context context);

		public abstract VisualNodePart getVisual();

		public abstract void addBrickListener(Context context, final VisualAttachmentAdapter.BoundsListener listener);

		public abstract void removeBrickListener(
				Context context, final VisualAttachmentAdapter.BoundsListener listener
		);
	}

	public static abstract class Hoverable {
		protected abstract void clear(Context context);

		public abstract void click(Context context);

		public abstract NodeType.NodeTypeVisual node();

		public abstract VisualNodePart part();
	}

	public static class BannerMessage {

		public Duration duration;
		public int priority = 0;
		public String text;
	}

	public class HoverIdle extends IdleTask {
		public Vector point = null;
		Context context;
		Brick at;

		@Override
		protected int priority() {
			return 500;
		}

		public HoverIdle(final Context context) {
			this.context = context;
			at = hoverBrick == null ? (
					context.wall.children.get(0).children.isEmpty() ?
							null :
							context.wall.children.get(0).children.get(0)
			) : hoverBrick;
		}

		@Override
		public void runImplementation() {
			// TODO store indexes rather than brick ref
			if (at == null) {
				hoverIdle = null;
				return;
			}
			if (point == null) {
				if (hover != null) {
					clearHover();
				}
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
				hover = at.hover(context, point);
				if (hover != old) {
					if (old != null)
						old.clear(context);
					ImmutableSet.copyOf(hoverListeners).forEach(l -> l.hoverChanged(context, hover));
				}
				hoverBrick = at;
				hoverIdle = null;
				return;
			}
			addIdle(this);
		}

		@Override
		protected void destroyed() {
			hoverIdle = null;
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
			final Syntax syntax,
			final Document document,
			final Consumer<IdleTask> addIdle,
			final Wall wall,
			final Iterable<Action> globalActions,
			final History history
	) {
		this.syntax = syntax;
		this.document = document;
		this.addIdle = addIdle;
		this.wall = wall;
		this.globalActions = globalActions;
		this.history = history;
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

	public static TheInterpolator interpolator = new TheInterpolator();

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
		translate(node, vector, false);
	}

	public void translate(final javafx.scene.Node node, final Vector vector, final boolean animate) {
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
		if (animate) {
			final double diffX = x - node.getLayoutX();
			final double diffY = y - node.getLayoutY();
			new Transition() {
				{
					setCycleDuration(javafx.util.Duration.millis(200));
				}

				@Override
				protected void interpolate(final double frac) {
					final double frac2 = Math.pow(1 - frac, 0.7);
					node.setTranslateX(-frac2 * diffX);
					node.setTranslateY(-frac2 * diffY);
					System.out.format("interp x %s y %s\n", node.getTranslateX(), node.getTranslateY());
				}
			}.play();
		}
		node.setLayoutX(x);
		node.setLayoutY(y);
	}
}
