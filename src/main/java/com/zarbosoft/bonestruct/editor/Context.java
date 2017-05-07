package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.InvalidDocument;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.banner.Banner;
import com.zarbosoft.bonestruct.editor.details.Details;
import com.zarbosoft.bonestruct.editor.display.Display;
import com.zarbosoft.bonestruct.editor.display.Group;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.history.Change;
import com.zarbosoft.bonestruct.editor.history.History;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeBase;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeType;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Wall;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.bonestruct.syntax.modules.Module;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.luxem.read.InvalidStream;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.rendaw.common.WeakCache;
import javafx.animation.Interpolator;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.pcollections.TreePVector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Context {
	public final History history;
	WeakCache<Set<Visual.Tag>, Style.Baked> styleCache = new WeakCache<>(v -> v.tags);
	public Visual window;
	private final Set<SelectionListener> selectionListeners = new HashSet<>();
	private final Set<HoverListener> hoverListeners = new HashSet<>();
	private final Set<TagsListener> selectionTagsChangeListeners = new HashSet<>();
	public final TransverseExtentsAdapter selectionExtentsAdapter;
	public List<Module.State> modules;
	public PSet<Visual.Tag> globalTags = HashTreePSet.empty();
	public List<KeyListener> keyListeners = new ArrayList<>();
	List<ContextIntListener> converseEdgeListeners = new ArrayList<>();
	List<ContextIntListener> transverseEdgeListeners = new ArrayList<>();
	public Map<Object, List<Action>> actions = new HashMap<>();
	public ClipboardEngine clipboardEngine;
	public final Wall foreground;
	public Group midground;
	public Group background;
	public Brick cornerstone;
	public int cornerstoneTransverse;
	public Banner banner;
	public Details details;
	public final Display display;
	int scrollStart;
	int scrollEnd;
	int scrollStartBeddingBefore;
	int scrollStartBeddingAfter;
	public int scroll;

	public static interface ContextIntListener {
		void changed(Context context, int oldValue, int newValue);
	}

	public void addConverseEdgeListener(final ContextIntListener listener) {
		converseEdgeListeners.add(listener);
	}

	public void removeConverseEdgeListener(final ContextIntListener listener) {
		converseEdgeListeners.remove(listener);
	}

	public void addTransverseEdgeListener(final ContextIntListener listener) {
		transverseEdgeListeners.add(listener);
	}

	public void removeTransverseEdgeListener(final ContextIntListener listener) {
		transverseEdgeListeners.remove(listener);
	}

	public void suggestCreateBricksBetween(final Brick previousBrick, final Brick nextBrick) {
		if (previousBrick == null || nextBrick == null)
			return;
		fillFromEndBrick(previousBrick);
	}

	@FunctionalInterface
	public interface KeyListener {
		boolean handleKey(Context context, HIDEvent event);
	}

	public void copy(final List<com.zarbosoft.bonestruct.document.Node> nodes) {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final RawWriter writer = new RawWriter(stream);
		for (final com.zarbosoft.bonestruct.document.Node node : nodes) {
			Document.write(node, writer);
		}
		clipboardEngine.set(stream.toByteArray());
	}

	public void copy(final String string) {
		clipboardEngine.setString(string);
	}

	public List<com.zarbosoft.bonestruct.document.Node> uncopy(final String type) {
		final byte[] bytes = clipboardEngine.get();
		if (bytes == null)
			return ImmutableList.of();
		try {
			return new Parse<com.zarbosoft.bonestruct.document.Node>()
					.grammar(syntax.getGrammar())
					.node(type)
					.parse(new ByteArrayInputStream(bytes))
					.collect(Collectors.toList());
		} catch (final InvalidStream e) {

		} catch (final InvalidDocument e) {

		}
		return ImmutableList.of();
	}

	public String uncopyString() {
		return clipboardEngine.getString();
	}

	public void changeGlobalTags(final Visual.TagsChange change) {
		globalTags = globalTags.minusAll(change.remove).plusAll(change.add);
		if (hover != null)
			hover.globalTagsChanged(this);
		if (selection != null)
			selection.globalTagsChanged(this);
		selectionTagsChanged();
		foreground.children.forEach(course -> course.children.forEach(brick -> brick.getVisual().tagsChanged(this)));
	}

	public void selectionTagsChanged() {
		if (selection == null)
			return;
		banner.tagsChanged(this);
		details.tagsChanged(this);
		selectionTagsChangeListeners.forEach(listener -> listener.tagsChanged(this, selection.getVisual().tags(this)));
	}

	public Object locateLong(final Path path) {
		return locate(path, true);
	}

	public Object locateShort(final Path path) {
		return locate(path, false);
	}

	/**
	 * Locate a Node or Value from a path.  If the path ends between those two, the last valid value
	 * is returned.  If the path references an invalid location InvalidPath is thrown.
	 * <p>
	 * If goLong is true, find the deepest element that resolves with this path.  If false, the shallowest.
	 *
	 * @param path
	 * @return
	 */
	public Object locate(final Path path, final boolean goLong) {
		int pathIndex = -1;
		final List<String> segments = ImmutableList.copyOf(path.segments);
		// Either (value) or (node & part) are always set
		Value value = document.top;
		com.zarbosoft.bonestruct.document.Node node = null;
		BackPart part = null;
		for (int cycle = 0; cycle < 10000; ++cycle) {
			if (part != null) {
				// Process from either the root or a sublevel of a node
				String middle = null;
				while (true) {
					if (part instanceof BackArray) {
						pathIndex += 1;
						if (pathIndex == segments.size())
							return node;
						final String segment = segments.get(pathIndex);
						final int tempPathIndex = pathIndex;
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
						pathIndex += 1;
						if (pathIndex >= segments.size())
							return node;
						final String segment = segments.get(pathIndex);
						final int tempPathIndex = pathIndex;
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
					} else if (part instanceof BackType) {
						part = ((BackType) part).child;
					} else
						return node;
				}
				value = node.data.get(middle);
				if (!goLong && pathIndex + 1 == segments.size())
					return value;
				part = null;
				node = null;
			} else {
				// Start from a value
				if (value instanceof ValueArray) {
					pathIndex += 1;
					if (pathIndex == segments.size())
						return value;
					final int tempPathIndex = pathIndex;
					final String segment = segments.get(pathIndex);
					if (((ValueArray) value).middle() instanceof MiddleRecord) {
						node = ((ValueArray) value).get().stream().filter(child -> (
								(ValuePrimitive) child.data.get((
										(BackDataKey) child.type.back().get(0)
								).middle)
						).get().equals(segment)).findFirst().orElseThrow(() -> new InvalidPath(String.format(
								"Invalid key %s at [%s].",
								segment,
								new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
						)));
						if (!goLong && pathIndex + 1 == segments.size())
							return node;
						part = node.type.back().get(1);
					} else if (((ValueArray) value).middle() instanceof MiddleArray) {
						final int index;
						try {
							index = Integer.parseInt(segment);
						} catch (final NumberFormatException e) {
							throw new InvalidPath(String.format("Segment [%s] at [%s] is not an integer.",
									segment,
									new Path(TreePVector.from(segments.subList(0, pathIndex)))
							));
						}
						node = ((ValueArray) value).get().stream().filter(child -> (
								((ValueArray.ArrayParent) child.parent).actualIndex <= index
						)).reduce((a, b) -> b).orElseThrow(() -> new InvalidPath(String.format(
								"Invalid index %d at [%s].",
								index,
								new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
						)));
						if (!goLong && pathIndex + 1 == segments.size())
							return node;
						part = node.type.back().get(index - ((ValueArray.ArrayParent) node.parent).actualIndex);
					}
				} else if (value instanceof ValueNode) {
					node = ((ValueNode) value).get();
					part = node.type.back().get(0);
				} else if (value instanceof ValuePrimitive) {
					if (segments.size() > pathIndex + 1)
						throw new InvalidPath(String.format("Path continues but data ends at primitive [%s].",
								new Path(TreePVector.from(segments.subList(0, pathIndex)))
						));
					return value;
				}
				value = null;
			}
		}
		throw new AssertionError("Path locate did not complete in a reasonable number of iterations.");
	}

	public abstract static class SelectionListener {

		public abstract void selectionChanged(Context context, Selection selection);
	}

	public abstract static class HoverListener {

		public abstract void hoverChanged(Context context, Hoverable selection);
	}

	public abstract static class TagsListener {

		public abstract void tagsChanged(Context context, PSet<Visual.Tag> tags);
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

	public void addSelectionTagsChangeListener(final TagsListener listener) {
		this.selectionTagsChangeListeners.add(listener);
	}

	public void removeSelectionTagsChangeListener(final TagsListener listener) {
		this.selectionTagsChangeListeners.remove(listener);
	}

	public void addKeyListener(final KeyListener listener) {
		this.keyListeners.add(listener);
	}

	public void removeKeyListener(final KeyListener listener) {
		this.keyListeners.remove(listener);
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
		if (hoverIdle != null) {
			hoverIdle.destroy();
		}
	}

	public class IdleFill extends IdleTask {
		public Deque<Brick> ends = new ArrayDeque<>();
		public Deque<Brick> starts = new ArrayDeque<>();

		@Override
		protected int priority() {
			return 100;
		}

		@Override
		public boolean runImplementation() {
			if (ends.isEmpty() && starts.isEmpty()) {
				idleFill = null;
				return false;
			}
			if (!ends.isEmpty()) {
				final Brick next = ends.pollLast();
				if (next.parent != null) {
					final Brick created = next.createNext(Context.this);
					if (created != null) {
						next.addAfter(Context.this, created);
						ends.addLast(created);
					}
				}
			}
			if (!starts.isEmpty()) {
				final Brick previous = starts.pollLast();
				if (previous.parent != null) {
					final Brick created = previous.createPrevious(Context.this);
					if (created != null) {
						previous.addBefore(Context.this, created);
						starts.addLast(created);
					}
				}
			}
			return true;
		}

		@Override
		protected void destroyed() {
			idleFill = null;
		}
	}

	public IdleFill idleFill = null;

	public class IdleClear extends IdleTask {
		public Brick end = null;
		public Brick start = null;

		@Override
		protected int priority() {
			return 105;
		}

		@Override
		protected boolean runImplementation() {
			if (end != null) {
				final Brick current = end;
				end = current.next();
				current.destroy(Context.this);
			}
			if (start != null) {
				final Brick current = start;
				start = current.previous();
				current.destroy(Context.this);
			}
			if (end == null && start == null) {
				idleClear = null;
				return false;
			} else
				return true;
		}

		@Override
		protected void destroyed() {
			idleClear = null;
		}
	}

	public IdleClear idleClear = null;

	public IdleTask idleClick = null;

	private void window(Visual visual) {
		if (visual == null) {
			visual = document.top.visual;
			changeGlobalTags(new Visual.TagsChange(ImmutableSet.of(new Visual.StateTag("unwindowed")),
					ImmutableSet.of(new Visual.StateTag("windowed"))
			));
		} else {
			changeGlobalTags(new Visual.TagsChange(ImmutableSet.of(new Visual.StateTag("windowed")),
					ImmutableSet.of(new Visual.StateTag("unwindowed"))
			));
		}
		final Brick first = visual.getFirstBrick(this);
		final Brick last = visual.getLastBrick(this);
		if (first != null || last != null) {
			if (idleClear == null) {
				idleClear = new IdleClear();
				addIdle(idleClear);
			}
			if (first != null)
				idleClear.start = first.previous();
			if (last != null)
				idleClear.end = last.next();
		}
		window = visual;
		window.anchor(this, ImmutableMap.of(), 0);
	}

	public void window(final Node node) {
		window(node.getVisual());
		node.getVisual().selectDown(this);
	}

	public void setSelection(final Selection selection) {
		final Selection oldSelection = this.selection;
		this.selection = selection;

		final VisualPart visual = this.selection.getVisual();
		{
			boolean inWindow = false;
			Visual mostDistantWindowableAncestor = null;
			Visual at = visual;
			if (window == at)
				inWindow = true;
			VisualParent parent = at.parent();
			int depth = 0;
			int limit = Integer.MAX_VALUE;
			while (parent != null) {
				at = parent.getTarget();
				if (window == at)
					inWindow = true;
				if (at instanceof VisualNodeType) {
					mostDistantWindowableAncestor = at;
					if (depth >= limit) {
						break;
					}
					depth += ((VisualNodeType) at).getType().depthScore;
				} else if (at instanceof VisualArray) {
					limit = Math.min(limit, depth + ((VisualArray) at).ellipsizeThreshold());
				} else if (at instanceof VisualNodeBase) {
					limit = Math.min(limit, depth + ((VisualNodeBase) at).ellipsizeThreshold());
				}
				parent = at.parent();
			}
			if (!inWindow) {
				window(mostDistantWindowableAncestor);
				final Brick firstBrick = window.getFirstBrick(this);
				final Brick lastBrick = window.getLastBrick(this);
				if (firstBrick != null && lastBrick != null) {
					fillFromStartBrick(firstBrick);
					fillFromEndBrick(lastBrick);
				}
			}
		}

		cornerstone = visual.getFirstBrick(this);
		if (cornerstone == null) {
			cornerstone = visual.createFirstBrick(this);
			if (cornerstone == null)
				throw new AssertionError();
			cornerstoneTransverse = 0;
		} else {
			cornerstoneTransverse = cornerstone.parent.transverseStart;
		}
		selection.addBrickListener(this, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				foreground.setCornerstone(context, brick);
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {

			}
		});
		ImmutableSet.copyOf(selectionListeners).forEach(l -> l.selectionChanged(this, selection));

		selection.addBrickListener(this, selectionExtentsAdapter.boundsListener);
		fillFromEndBrick(cornerstone);
		fillFromStartBrick(cornerstone);

		selectionTagsChanged();
		if (oldSelection != null) {
			oldSelection.clear(this);
		}
	}

	public Style.Baked getStyle(final Set<Visual.Tag> tags) {
		return styleCache.getOrCreate(tags, tags1 -> {
			final Style.Baked out = new Style.Baked(tags);
			for (final Style style : syntax.styles) {
				if (!tags.containsAll(style.with) || !Sets.intersection(tags, style.without).isEmpty())
					continue;
				out.merge(style);
			}
			return out;
		});
	}

	public class HoverIdle extends IdleTask {
		public com.zarbosoft.bonestruct.editor.visual.Vector point = null;
		Context context;
		Brick at;

		@Override
		protected int priority() {
			return 500;
		}

		public HoverIdle(final Context context) {
			this.context = context;
			at = hoverBrick == null ? (
					context.foreground.children.get(0).children.isEmpty() ?
							null :
							context.foreground.children.get(0).children.get(0)
			) : hoverBrick;
		}

		@Override
		public boolean runImplementation() {
			// TODO store indexes rather than brick ref
			if (at == null || at.parent == null) {
				hoverIdle = null;
				return false;
			}
			if (point == null) {
				if (hover != null) {
					clearHover();
				}
				hoverBrick = null;
				hoverIdle = null;
				return false;
			}
			if (point.transverse < at.parent.transverseStart && at.parent.index > 0) {
				at = context.foreground.children.get(at.parent.index - 1).children.get(0);
			} else if (point.transverse > at.parent.transverseEdge(context) &&
					at.parent.index < foreground.children.size() - 1) {
				at = context.foreground.children.get(at.parent.index + 1).children.get(0);
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
				return false;
			}
			return true;
		}

		@Override
		protected void destroyed() {
			hoverIdle = null;
		}
	}

	public final Syntax syntax;
	public final Document document;
	public int edge;
	public int transverseEdge;
	public Brick hoverBrick;
	public Hoverable hover;
	public HoverIdle hoverIdle;
	public Selection selection;

	public Context(
			final Syntax syntax,
			final Document document,
			final Display display,
			final Consumer<IdleTask> addIdle,
			final History history
	) {
		actions.put(this, ImmutableList.of(new Action() {
			@Override
			public void run(final Context context) {
				if (window == document.top.getVisual())
					return;
				window(document.top.getVisual());
			}

			@Override
			public String getName() {
				return "window_clear";
			}
		}));
		this.syntax = syntax;
		this.document = document;
		this.display = display;
		edge = display.edge(this);
		transverseEdge = display.transverseEdge(this);
		background = display.group();
		midground = display.group();
		banner = new Banner(this);
		details = new Details(this);
		this.addIdle = addIdle;
		this.foreground = new Wall(this);
		this.history = history;
		display.addConverseEdgeListener((oldValue, newValue) -> {
			edge = Math.max(0, newValue - document.syntax.padConverse * 2);
			if (newValue < oldValue) {
				foreground.idleCompact(this);
			} else if (newValue > oldValue) {
				foreground.idleExpand(this);
			}
			converseEdgeListeners.forEach(listener -> listener.changed(this, oldValue, newValue));
		});
		display.addTransverseEdgeListener((
				(oldValue, newValue) -> {
					transverseEdge = Math.max(0, newValue - document.syntax.padTransverse * 2);
					scrollVisible();
					transverseEdgeListeners.forEach(listener -> listener.changed(this, oldValue, newValue));
				}
		));
		history.addListener(new History.Listener() {
			@Override
			public void applied(final Context context, final Change change) {
				if (hoverIdle != null) {
					hoverIdle.destroy();
				}
			}
		});
		selectionExtentsAdapter = new TransverseExtentsAdapter(this);
		selectionExtentsAdapter.addListener(this, new TransverseExtentsAdapter.Listener() {
			@Override
			public void transverseChanged(final Context context, final int transverse) {
				scrollStart = transverse;
				scrollVisible();
			}

			@Override
			public void transverseEdgeChanged(final Context context, final int transverse) {
				scrollEnd = transverse;
				scrollVisible();
			}

			@Override
			public void beddingAfterChanged(final Context context, final int beddingAfter) {
				scrollStartBeddingAfter = beddingAfter;
				scrollVisible();
			}

			@Override
			public void beddingBeforeChanged(final Context context, final int beddingBefore) {
				scrollStartBeddingBefore = beddingBefore;
				scrollVisible();
			}
		});
	}

	private void scrollVisible() {
		final int minimum = scrollStart - scrollStartBeddingBefore - syntax.padTransverse;
		final int maximum = scrollEnd + scrollStartBeddingAfter + syntax.padTransverse;
		final int maxDiff = maximum - transverseEdge - scroll;
		Integer newScroll = null;
		if (minimum < scroll) {
			newScroll = minimum;
		} else if (maxDiff > 0 && scroll + maxDiff < minimum) {
			newScroll = scroll + maxDiff;
		}
		if (newScroll != null) {
			foreground.visual.setPosition(this,
					new Vector(syntax.padConverse, -newScroll),
					syntax.animateCoursePlacement
			);
			background.setPosition(this, new Vector(syntax.padConverse, -newScroll), syntax.animateCoursePlacement);
			scroll = newScroll;
			banner.setScroll(this, newScroll);
			details.setScroll(this, newScroll);
		}
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

}
