package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.display.Display;
import com.zarbosoft.bonestruct.display.Group;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.banner.Banner;
import com.zarbosoft.bonestruct.editor.details.Details;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.history.History;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.bonestruct.syntax.modules.Module;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.Wall;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.write.RawWriter;
import javafx.animation.Interpolator;
import org.pcollections.TreePVector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Context {
	public final History history;
	public WeakHashMap<Set<Visual.Tag>, WeakReference<Style.Baked>> styleCache = new WeakHashMap<>();
	public VisualPart window;
	private final Set<SelectionListener> selectionListeners = new HashSet<>();
	private final Set<HoverListener> hoverListeners = new HashSet<>();
	private final Set<TagsListener> tagsChangeListeners = new HashSet<>();
	public final TransverseExtentsAdapter selectionExtentsAdapter = new TransverseExtentsAdapter();
	public List<Module.State> modules;
	public Set<Visual.Tag> globalTags = new HashSet<>();
	public List<KeyListener> keyListeners = new ArrayList<>();
	public Map<Object, List<Action>> actions = new HashMap<>();
	public ClipboardEngine clipboardEngine;
	public final Wall wall;
	public Group background;
	public Brick cornerstone;
	public int cornerstoneTransverse;
	public int scrollTransverse;
	public Banner banner;
	public Details details;
	public final Display display;

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

	public List<com.zarbosoft.bonestruct.document.Node> uncopy(final String type) {
		final byte[] bytes = clipboardEngine.get();
		if (bytes == null)
			return ImmutableList.of();
		return new Parse<com.zarbosoft.bonestruct.document.Node>()
				.grammar(syntax.getGrammar())
				.node(type)
				.parse(new ByteArrayInputStream(bytes))
				.collect(Collectors.toList());
	}

	public void changeGlobalTags(final Visual.TagsChange change) {
		globalTags.removeAll(change.remove);
		globalTags.addAll(change.add);
		selectionTagsChanged();
		wall.children.forEach(course -> course.children.forEach(brick -> brick.getVisual().tagsChanged(this)));
	}

	public void selectionTagsChanged() {
		if (selection == null)
			return;
		tagsChangeListeners.forEach(listener -> listener.tagsChanged(this, selection.getVisual().tags(this)));
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
							throw new InvalidPath(String.format(
									"Segment [%s] at [%s] is not an integer.",
									segment,
									new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
							));
						}
						final BackArray arrayPart = ((BackArray) part);
						if (subIndex >= arrayPart.elements.size())
							throw new InvalidPath(String.format(
									"Invalid index %d at [%s].",
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
							throw new InvalidPath(String.format(
									"Invalid key [%s] at [%s].",
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
							throw new InvalidPath(String.format(
									"Segment [%s] at [%s] is not an integer.",
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
						throw new InvalidPath(String.format(
								"Path continues but data ends at primitive [%s].",
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

		public abstract void tagsChanged(Context context, Set<Visual.Tag> tags);
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

	public void addTagsChangeListener(final TagsListener listener) {
		this.tagsChangeListeners.add(listener);
	}

	public void removeTagsChangeListener(final TagsListener listener) {
		this.tagsChangeListeners.remove(listener);
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
		final Selection oldSelection = this.selection;
		this.selection = selection;

		final VisualPart visual = this.selection.getVisual();
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
		selection.addBrickListener(this, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				wall.setCornerstone(context, brick);
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
			final Display display,
			final Consumer<IdleTask> addIdle,
			final History history
	) {
		this.syntax = syntax;
		this.document = document;
		this.display = display;
		background = display.group();
		banner = new Banner(this);
		details = new Details(this);
		this.addIdle = addIdle;
		this.wall = new Wall(this);
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

}
