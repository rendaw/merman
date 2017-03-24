package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.ChainComparator;
import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.InvalidPath;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.model.*;
import com.zarbosoft.bonestruct.editor.model.back.*;
import com.zarbosoft.bonestruct.editor.model.middle.*;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.raw.RawText;
import com.zarbosoft.bonestruct.editor.visual.raw.RawTextUtils;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Attachment;
import com.zarbosoft.bonestruct.editor.visual.wall.Bedding;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.bonestruct.editor.visual.wall.Wall;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.pcollections.TreePVector;

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
	private final Iterable<Action> globalActions;
	public VisualNodePart window;
	private final Set<SelectionListener> selectionListeners = new HashSet<>();
	private final Set<HoverListener> hoverListeners = new HashSet<>();
	public final TransverseExtentsAdapter selectionExtentsAdapter = new TransverseExtentsAdapter();
	public List<Plugin.State> plugins;

	public static class Display {

		public final Wall wall;
		public Group background;
		public Brick cornerstone;
		public int cornerstoneTransverse;
		public int scrollTransverse;
		public Banner banner;
		public Details details;

		public Display(final Wall wall) {
			this.wall = wall;
		}
	}

	public Display display = null;

	public int sceneGetConverseSpan(final Node node) {
		switch (syntax.converseDirection) {
			case UP:
			case DOWN:
				return (int) node.getLayoutBounds().getHeight();
			case LEFT:
			case RIGHT:
				return (int) node.getLayoutBounds().getWidth();
		}
		throw new AssertionError("DEAD CODE");
	}

	public int sceneGetTransverseSpan(final Node node) {
		switch (syntax.transverseDirection) {
			case UP:
			case DOWN:
				return (int) node.getLayoutBounds().getHeight();
			case LEFT:
			case RIGHT:
				return (int) node.getLayoutBounds().getWidth();
		}
		throw new AssertionError("DEAD CODE");
	}

	public int sceneGetConverse(final Node node) {
		switch (syntax.converseDirection) {
			case UP:
				return edge - (int) node.getLayoutY() + (int) node.getLayoutBounds().getWidth();
			case DOWN:
				return (int) node.getLayoutY();
			case LEFT:
				return edge - (int) node.getLayoutX() + (int) node.getLayoutBounds().getWidth();
			case RIGHT:
				return (int) node.getLayoutX();
		}
		throw new AssertionError("DEAD CODE");
	}

	public int sceneGetTransverse(final Node node) {
		switch (syntax.transverseDirection) {
			case UP:
				return transverseEdge - (int) node.getLayoutY() + (int) node.getLayoutBounds().getWidth();
			case DOWN:
				return (int) node.getLayoutY();
			case LEFT:
				return transverseEdge - (int) node.getLayoutX() + (int) node.getLayoutBounds().getWidth();
			case RIGHT:
				return (int) node.getLayoutX();
		}
		throw new AssertionError("DEAD CODE");
	}

	public Vector sceneGet(final Node node) {
		final Bounds bounds = node.getLayoutBounds();
		int converse = 0;
		int transverse = 0;
		switch (syntax.converseDirection) {
			case UP:
				converse = edge - (int) node.getLayoutY() + (int) bounds.getWidth();
				break;
			case DOWN:
				converse = (int) node.getLayoutY();
				break;
			case LEFT:
				converse = edge - (int) node.getLayoutX() + (int) bounds.getWidth();
				break;
			case RIGHT:
				converse = (int) node.getLayoutX();
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				transverse = transverseEdge - (int) node.getLayoutY() + (int) bounds.getWidth();
				break;
			case DOWN:
				transverse = (int) node.getLayoutY();
				break;
			case LEFT:
				transverse = transverseEdge - (int) node.getLayoutX() + (int) bounds.getWidth();
				break;
			case RIGHT:
				transverse = (int) node.getLayoutX();
				break;
		}
		return new Vector(converse, transverse);
	}

	/**
	 * Locate a Node or DataElement.Value from a path.  If the path ends between those two, the last valid value
	 * is returned.  If the path references an invalid location InvalidPath is thrown.
	 *
	 * @param path
	 * @return
	 */
	public Object locate(final Path path) {
		int pathIndex = -1;
		final List<String> segments = ImmutableList.copyOf(path.segments);
		DataElement.Value value = document.top;
		com.zarbosoft.bonestruct.editor.model.Node node = null;
		BackPart part = null;
		while (true) {
			if (part != null) {
				// Process from either the root or a sublevel of a node
				String middle = null;
				while (true) {
					final String segment = segments.get(++pathIndex);
					final int tempPathIndex = pathIndex;
					if (segment == null)
						return node;
					if (part instanceof BackArray) {
						final int subIndex;
						try {
							subIndex = Integer.parseInt(segment);
						} catch (final NumberFormatException e) {
							throw new InvalidPath(String.format("Segment [%s] at [%s] is not an integer.",
									segment,
									new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
							));
						}
						final BackArray arrayPart = ((BackArray) part);
						if (subIndex >= arrayPart.elements.size())
							throw new InvalidPath(String.format("Invalid index %d at [%s].",
									subIndex,
									new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
							));
						part = arrayPart.elements.get(subIndex);
					} else if (part instanceof BackRecord) {
						final BackRecord recordPart = ((BackRecord) part);
						if (!recordPart.pairs.containsKey(segment))
							throw new InvalidPath(String.format("Invalid key [%s] at [%s].",
									segment,
									new Path(TreePVector.from(segments.subList(0, pathIndex)))
							));
						part = recordPart.pairs.get(segment);
					} else if (part instanceof BackDataArray) {
						middle = ((BackDataArray) part).middle;
						break;
					} else if (part instanceof BackDataKey) {
						middle = ((BackDataKey) part).middle;
						break;
					} else if (part instanceof BackDataNode) {
						middle = ((BackDataNode) part).middle;
						break;
					} else if (part instanceof BackDataPrimitive) {
						middle = ((BackDataPrimitive) part).middle;
						break;
					} else if (part instanceof BackDataRecord) {
						middle = ((BackDataRecord) part).middle;
						break;
					} else
						return node;
				}
				value = node.data.get(middle);
				part = null;
				node = null;
			} else {
				// Start from a value
				if (value instanceof DataArrayBase.Value) {
					if (((DataArrayBase.Value) value).data() instanceof DataRecord) {
						final String segment = segments.get(++pathIndex);
						if (segment == null)
							return null;
						final int tempPathIndex = pathIndex;
						node = ((DataArrayBase.Value) value).get().stream().filter(child -> (
								(DataRecordKey.Value) child.data.get((
										(BackDataKey) child.type.back().get(0)
								).middle)
						).get().equals(segment)).findFirst().orElseThrow(() -> new InvalidPath(String.format(
								"Invalid key %s at [%s].",
								segment,
								new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
						)));
						part = (BackDataNode) node.type.back().get(1);
					} else if (((DataArrayBase.Value) value).data() instanceof DataArray) {

						final String segment = segments.get(++pathIndex);
						if (segment == null)
							return null;
						final int index;
						try {
							index = Integer.parseInt(segment);
						} catch (final NumberFormatException e) {
							throw new InvalidPath(String.format("Segment [%s] at [%s] is not an integer.",
									segment,
									new Path(TreePVector.from(segments.subList(0, pathIndex)))
							));
						}
						final int tempPathIndex = pathIndex;
						node = ((DataArrayBase.Value) value).get().stream().filter(child -> (
								((DataArrayBase.Value.ArrayParent) child.parent).actualIndex <= index
						)).findFirst().orElseThrow(() -> new InvalidPath(String.format("Invalid index %d at [%s].",
								index,
								new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
						)));
						part = node.type
								.back()
								.get(((DataArrayBase.Value.ArrayParent) node.parent).actualIndex - index);
					}
				} else if (value instanceof DataNode.Value) {
					node = ((DataNode.Value) value).get();
					part = node.type.back().get(0);
				} else if (value instanceof DataPrimitive.Value) {
					if (!segments.isEmpty())
						throw new InvalidPath(String.format("Path continues but data ends at primitive [%s].",
								new Path(TreePVector.from(segments.subList(0, pathIndex)))
						));
					return value;
				}
				value = null;
			}
		}
	}

	public static class Banner {
		public RawText text;
		private final PriorityQueue<BannerMessage> queue =
				new PriorityQueue<>(11, new ChainComparator<BannerMessage>().greaterFirst(m -> m.priority).build());
		private BannerMessage current;
		private final Timer timer = new Timer();
		private Brick brick;
		private int transverse;
		private int scroll;
		private Bedding bedding;
		private IdlePlace idle;
		private final Attachment attachment = new Attachment() {
			@Override
			public void setTransverse(final Context context, final int transverse) {
				Banner.this.transverse = transverse;
				idlePlace(context);
			}

			@Override
			public void destroy(final Context context) {
				brick = null;
			}
		};

		private void idlePlace(final Context context) {
			if (text == null)
				return;
			if (idle == null) {
				idle = new IdlePlace(context);
				context.addIdle(idle);
			}
		}

		public void setScroll(final Context context, final int scroll) {
			this.scroll = scroll;
			idlePlace(context);
		}

		private class IdlePlace extends IdleTask {
			private final Context context;

			private IdlePlace(final Context context) {
				this.context = context;
			}

			@Override
			protected void runImplementation() {
				if (text != null) {
					text.setTransverse(context,
							Math.max(scroll, Banner.this.transverse - (int) RawTextUtils.getDescent(text.getFont())),
							false
					);
				}
				idle = null;
			}

			@Override
			protected void destroyed() {
				idle = null;
			}
		}

		public Banner(final Context context) {
			context.addSelectionListener(new SelectionListener() {
				@Override
				public void selectionChanged(final Context context, final Selection selection) {
					selection.addBrickListener(context, new VisualAttachmentAdapter.BoundsListener() {
						@Override
						public void firstChanged(final Context context, final Brick first) {
							if (brick != null) {
								if (bedding != null)
									brick.removeBedding(context, bedding);
								brick.removeAttachment(context, attachment);
							}
							brick = first;
							if (bedding != null) {
								brick.addBedding(context, bedding);
							}
							brick.addAttachment(context, attachment);
						}

						@Override
						public void lastChanged(final Context context, final Brick last) {

						}
					});
				}
			});
		}

		public void addMessage(final Context context, final BannerMessage message) {
			if (queue.isEmpty()) {
				text = new RawText(context, context.getStyle(ImmutableSet.of(new VisualNode.PartTag("banner"))));
				context.display.background.getChildren().add(text.getVisual());
				text.setTransverse(context,
						Math.max(scroll, transverse - (int) RawTextUtils.getDescent(text.getFont())),
						false
				);
				bedding = new Bedding(text.transverseSpan(context), 0);
				brick.addBedding(context, bedding);
			}
			queue.add(message);
			update(context);
		}

		private void update(final Context context) {
			if (queue.isEmpty()) {
				if (text != null) {
					context.display.background.getChildren().remove(text.getVisual());
					text = null;
					brick.removeBedding(context, bedding);
					bedding = null;
				}
			} else if (queue.peek() != current) {
				current = queue.peek();
				text.setText(context, current.text);
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

		if (display != null) {
			final Brick newCornerstone = visual.getFirstBrick(this);
			if (newCornerstone == null) {
				display.cornerstone = visual.createFirstBrick(this);
				display.cornerstoneTransverse = 0;
			} else {
				display.cornerstone = newCornerstone;
				display.cornerstoneTransverse = newCornerstone.parent.transverseStart;
			}
			selection.addBrickListener(this, new VisualAttachmentAdapter.BoundsListener() {
				@Override
				public void firstChanged(final Context context, final Brick brick) {
					display.wall.setCornerstone(context, brick);
				}

				@Override
				public void lastChanged(final Context context, final Brick brick) {

				}
			});
			ImmutableSet.copyOf(selectionListeners).forEach(l -> l.selectionChanged(this, selection));
			selection.addBrickListener(this, selectionExtentsAdapter.boundsListener);

			fillFromEndBrick(display.cornerstone);
			fillFromStartBrick(display.cornerstone);

			hotkeyGrammar = new Grammar();
			final Union union = new Union();
			for (final Action action : Iterables.concat(selection.getActions(this), globalActions)) {
				final List<com.zarbosoft.bonestruct.editor.model.pidgoon.Node> hotkeyEntries =
						selection.getHotkeys(this).hotkeys.get(action.getName());
				if (hotkeyEntries == null)
					continue;
				for (final com.zarbosoft.bonestruct.editor.model.pidgoon.Node hotkey : hotkeyEntries) {
					union.add(new BakedOperator(hotkey.build(), store -> store.pushStack(action)));
				}
			}
			hotkeyGrammar.add("root", union);
		}
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

		public abstract class VisualListener {

		}

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
					context.display.wall.children.get(0).children.isEmpty() ?
							null :
							context.display.wall.children.get(0).children.get(0)
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
				at = context.display.wall.children.get(at.parent.index - 1).children.get(0);
			} else if (point.transverse > at.parent.transverseEdge(context) &&
					at.parent.index < display.wall.children.size() - 1) {
				at = context.display.wall.children.get(at.parent.index + 1).children.get(0);
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
		if (wall != null) {
			display = new Display(wall);
		}
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

	private class TransitionSmoothOut extends Transition {
		private final Node node;
		private final Double diffX;
		private final Double diffY;

		{
			setCycleDuration(javafx.util.Duration.millis(200));
		}

		private TransitionSmoothOut(final Node node, final Double diffX, final Double diffY) {
			this.node = node;
			this.diffX = diffX;
			this.diffY = diffY;
		}

		@Override
		protected void interpolate(final double frac) {
			final double frac2 = Math.pow(1 - frac, 3);
			if (diffX != null)
				node.setTranslateX(-frac2 * diffX);
			if (diffY != null)
				node.setTranslateY(-frac2 * diffY);
		}
	}

	public void translateTransverse(final javafx.scene.Node node, final int transverse, final boolean animate) {
		Integer x = null;
		Integer y = null;
		switch (syntax.transverseDirection) {
			case UP:
				y = (int) node.getLayoutBounds().getHeight() + transverse;
				break;
			case DOWN:
				y = transverse;
				break;
			case LEFT:
				x = (int) node.getLayoutBounds().getWidth() + transverse;
				break;
			case RIGHT:
				x = transverse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(node, x - node.getLayoutX(), null).play();
			node.setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(node, null, y - node.getLayoutY()).play();
			node.setLayoutY(y);
		}
	}

	public void translateConverse(final javafx.scene.Node node, final int converse, final boolean animate) {
		Integer x = null;
		Integer y = null;
		switch (syntax.converseDirection) {
			case UP:
				y = (int) node.getLayoutBounds().getHeight() + converse;
				break;
			case DOWN:
				y = converse;
				break;
			case LEFT:
				x = (int) node.getLayoutBounds().getWidth() + converse;
				break;
			case RIGHT:
				x = converse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(node, x - node.getLayoutX(), null).play();
			node.setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(node, null, y - node.getLayoutY()).play();
			node.setLayoutY(y);
		}
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
		if (animate)
			new TransitionSmoothOut(node, x - node.getLayoutX(), y - node.getLayoutY()).play();
		node.setLayoutX(x);
		node.setLayoutY(y);
	}
}
