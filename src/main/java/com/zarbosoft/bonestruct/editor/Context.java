package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.InvalidDocument;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
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
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.editor.wall.Attachment;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Wall;
import com.zarbosoft.bonestruct.modules.Module;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.FrontGapBase;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.last;

public class Context {
	public final History history;
	WeakCache<Set<Tag>, Style.Baked> styleCache = new WeakCache<>(v -> v.tags);
	public boolean window;
	public Atom windowAtom;
	private final Set<SelectionListener> selectionListeners = new HashSet<>();
	private final Set<HoverListener> hoverListeners = new HashSet<>();
	private final Set<TagsListener> selectionTagsChangeListeners = new HashSet<>();
	private final Set<ActionChangeListener> actionChangeListeners = new HashSet<>();
	public List<Module.State> modules;
	public PSet<Tag> globalTags = HashTreePSet.empty();
	public List<KeyListener> keyListeners = new ArrayList<>();
	List<ContextIntListener> converseEdgeListeners = new ArrayList<>();
	List<ContextIntListener> transverseEdgeListeners = new ArrayList<>();
	public List<GapChoiceListener> gapChoiceListeners = new ArrayList<>();
	private final Map<Object, List<Action>> actions = new HashMap<>();
	public ClipboardEngine clipboardEngine;
	/**
	 * Contains the cursor and other marks.  Scrolls.
	 */
	public final Group overlay;
	/**
	 * Contains the source code.  Scrolls.
	 */
	public final Wall foreground;
	/**
	 * Contains banner/details and icons.  Doesn't scroll.
	 */
	public Group midground;
	/**
	 * Contains source borders.  Scrolls.
	 */
	public Group background;
	public Banner banner;
	public Details details;
	public final Display display;
	int scrollStart;
	int scrollEnd;
	int scrollStartBeddingBefore;
	int scrollStartBeddingAfter;
	public int scroll;
	int selectToken = 0;
	boolean keyIgnore = false;

	public static PSet<Tag> asFreeTags(final Set<String> tags) {
		return HashTreePSet.from(tags.stream().map(tag -> new FreeTag(tag)).collect(Collectors.toList()));
	}

	public static interface GapChoiceListener {
		void changed(Context context, List<? extends FrontGapBase.Choice> choices);
	}

	public void addGapChoiceListener(final GapChoiceListener listener) {
		gapChoiceListeners.add(listener);
	}

	public void removeGapChoiceListener(final GapChoiceListener listener) {
		gapChoiceListeners.remove(listener);
	}

	public static interface ContextIntListener {
		void changed(Context context, int oldValue, int newValue);
	}

	public static interface ActionChangeListener {
		void actionsAdded(Context context);

		void actionsRemoved(Context context);
	}

	public void addActions(final Object key, final List<Action> actions) {
		this.actions.put(key, actions);
		ImmutableSet.copyOf(actionChangeListeners).forEach(listener -> listener.actionsAdded(this));
	}

	public void removeActions(final Object key) {
		this.actions.remove(key);
		ImmutableSet.copyOf(actionChangeListeners).forEach(listener -> listener.actionsRemoved(this));
	}

	public void addActionChangeListener(final ActionChangeListener listener) {
		actionChangeListeners.add(listener);
	}

	public void removeActionChangeListener(final ActionChangeListener listener) {
		actionChangeListeners.remove(listener);
	}

	public Stream<Action> actions() {
		return actions.entrySet().stream().flatMap(e -> e.getValue().stream());
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

	@FunctionalInterface
	public interface KeyListener {
		boolean handleKey(Context context, HIDEvent event);
	}

	public void copy(final List<Atom> atoms) {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		final RawWriter writer = new RawWriter(stream);
		for (final Atom atom : atoms) {
			Document.write(atom, writer);
		}
		clipboardEngine.set(stream.toByteArray());
	}

	public void copy(final String string) {
		clipboardEngine.setString(string);
	}

	public List<Atom> uncopy(final String type) {
		final byte[] bytes = clipboardEngine.get();
		if (bytes == null)
			return ImmutableList.of();
		try {
			return new Parse<Atom>()
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

	public void changeGlobalTags(final TagsChange change) {
		globalTags = change.apply(globalTags);
		if (windowAtom == null)
			document.root.visual.globalTagsChanged(this);
		else
			windowAtom.visual.globalTagsChanged(this);
	}

	public void selectionTagsChanged() {
		if (selection == null)
			return;
		banner.tagsChanged(this);
		details.tagsChanged(this);
		selectionTagsChangeListeners.forEach(listener -> listener.tagsChanged(this));
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
		// Either (value) or (atom & part) are always set
		Value value = document.rootArray;
		Atom atom = null;
		BackPart part = null;
		for (int cycle = 0; cycle < 10000; ++cycle) {
			if (part != null) {
				// Process from either the root or a sublevel of a atom
				String middle = null;
				while (true) {
					if (part instanceof BackArray) {
						pathIndex += 1;
						if (pathIndex == segments.size())
							return atom;
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
							return atom;
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
					} else if (part instanceof BackDataAtom) {
						middle = ((BackDataAtom) part).middle;
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
						return atom;
				}
				value = atom.data.get(middle);
				if (!goLong && pathIndex + 1 == segments.size())
					return value;
				part = null;
				atom = null;
			} else {
				// Start from a value
				if (value instanceof ValueArray) {
					pathIndex += 1;
					if (pathIndex == segments.size())
						return value;
					final int tempPathIndex = pathIndex;
					final String segment = segments.get(pathIndex);
					if (((ValueArray) value).middle() instanceof MiddleRecord) {
						atom = ((ValueArray) value).data.stream().filter(child -> (
								(ValuePrimitive) child.data.get((
										(BackDataKey) child.type.back().get(0)
								).middle)
						).get().equals(segment)).findFirst().orElseThrow(() -> new InvalidPath(String.format(
								"Invalid key %s at [%s].",
								segment,
								new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
						)));
						if (!goLong && pathIndex + 1 == segments.size())
							return atom;
						part = atom.type.back().get(1);
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
						atom = ((ValueArray) value).data.stream().filter(child -> (
								((ValueArray.ArrayParent) child.parent).actualIndex <= index
						)).reduce((a, b) -> b).orElseThrow(() -> new InvalidPath(String.format(
								"Invalid index %d at [%s].",
								index,
								new Path(TreePVector.from(segments.subList(0, tempPathIndex)))
						)));
						if (!goLong && pathIndex + 1 == segments.size())
							return atom;
						part = atom.type.back().get(index - ((ValueArray.ArrayParent) atom.parent).actualIndex);
					}
				} else if (value instanceof ValueAtom) {
					atom = ((ValueAtom) value).get();
					part = atom.type.back().get(0);
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

		public abstract void tagsChanged(Context context);
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

	private void idleLayBricksOutward() {
		idleLayBricksBeforeStart(foreground.children.get(0).children.get(0));
		idleLayBricksAfterEnd(last(last(foreground.children).children));
	}

	public void idleLayBricks(
			final VisualParent parent,
			final int index,
			final int addCount,
			final int size,
			final Function<Integer, Brick> accessFirst,
			final Function<Integer, Brick> accessLast,
			final Function<Integer, Brick> create
	) {
		if (size == 0)
			throw new AssertionError();
		if (index > 0) {
			final Brick previousBrick = accessLast.apply(index - 1);
			if (previousBrick != null) {
				idleLayBricksAfterEnd(previousBrick);
				return;
			}
			if (index + addCount < size) {
				// Hits neither edge
				final Brick nextBrick = accessFirst.apply(index + addCount);
				if (nextBrick == null)
					return;
				idleLayBricksBeforeStart(nextBrick);
			} else {
				// Hits end edge
				if (parent == null)
					return;
				final Brick nextBrick = parent.getNextBrick(this);
				if (nextBrick == null)
					return;
				idleLayBricksBeforeStart(nextBrick);
			}
		} else {
			if (index + addCount < size) {
				// Hits index edge
				final Brick nextBrick = accessFirst.apply(index + addCount);
				if (nextBrick != null) {
					idleLayBricksBeforeStart(nextBrick);
					return;
				}
				final Brick previousBrick = parent.getPreviousBrick(this);
				if (previousBrick == null)
					return;
				idleLayBricksAfterEnd(previousBrick);
			} else {
				// Hits both edges
				if (parent == null)
					return;
				final Brick previousBrick = parent.getPreviousBrick(this);
				if (previousBrick != null) {
					idleLayBricksAfterEnd(previousBrick);
					return;
				}
				final Brick nextBrick = parent.getNextBrick(this);
				if (nextBrick == null)
					return;
				idleLayBricksBeforeStart(nextBrick);
			}
		}
	}

	public void idleLayBricksAfterEnd(final Brick end) {
		if (idleLayBricks == null) {
			idleLayBricks = new IdleLayBricks();
			addIdle(idleLayBricks);
		}
		idleLayBricks.ends.add(end);
	}

	public void idleLayBricksBeforeStart(final Brick start) {
		if (idleLayBricks == null) {
			idleLayBricks = new IdleLayBricks();
			addIdle(idleLayBricks);
		}
		idleLayBricks.starts.add(start);
	}

	public void clearHover() {
		if (hover != null) {
			hover.clear(this);
			hover = null;
		}
		if (hoverIdle != null) {
			hoverIdle.destroy();
		}
	}

	public class IdleLayBricks extends IdleTask {
		public Set<Brick> ends = new HashSet<>();
		public Set<Brick> starts = new HashSet<>();

		@Override
		protected int priority() {
			return 155;
		}

		@Override
		public boolean runImplementation() {
			for (int i = 0; i < syntax.layBrickBatchSize; ++i) {
				if (ends.isEmpty() && starts.isEmpty()) {
					return false;
				}
				if (!ends.isEmpty()) {
					final Brick next = ends.iterator().next();
					ends.remove(next);
					if (next.parent != null) {
						final Brick created = next.createNext(Context.this);
						if (created != null) {
							next.addAfter(Context.this, created);
							ends.add(created);
						}
					}
				}
				if (!starts.isEmpty()) {
					final Brick previous = starts.iterator().next();
					starts.remove(previous);
					if (previous.parent != null) {
						final Brick created = previous.createPrevious(Context.this);
						if (created != null) {
							previous.addBefore(Context.this, created);
							starts.add(created);
						}
					}
				}
			}
			return true;
		}

		@Override
		protected void destroyed() {
			idleLayBricks = null;
		}
	}

	public IdleLayBricks idleLayBricks = null;

	private boolean overlapsWindow(final Visual visual) {
		final Visual stop = windowAtom == null ? document.root.visual : windowAtom.visual;
		Visual at = visual;
		while (true) {
			if (at == stop)
				return true;
			if (at.parent() == null)
				break;
			at = at.parent().visual();
		}
		return false;
	}

	public void createWindowForSelection(final Value value, final int depthThreshold) {
		final Visual oldWindow = windowAtom == null ? document.root.visual : windowAtom.visual;
		Visual windowVisual = null;

		// Try just going up
		if (windowAtom != null) {
			Value at = windowAtom.parent.value();
			while (true) {
				if (at == value) {
					windowAtom = at.parent.atom();
					windowVisual = windowAtom.createVisual(this, null, ImmutableMap.of(), 0);
				}
				final Atom atom = at.parent.atom();
				if (atom.parent == null)
					break;
				at = atom.parent.value();
			}
		}

		// Otherwise go up from the selection to find the highest parent where this is still visible
		if (windowVisual == null) {
			windowAtom = value.parent.atom();
			int depth = 0;
			while (true) {
				depth += windowAtom.type.depthScore;
				if (depth >= depthThreshold)
					break;
				if (windowAtom.parent == null)
					break;
				windowAtom = windowAtom.parent.value().parent.atom();
			}

			if (depth < depthThreshold) {
				windowAtom = null;
				windowVisual = document.root.createVisual(this, null, ImmutableMap.of(), 0);
			} else {
				windowVisual = windowAtom.createVisual(this, null, ImmutableMap.of(), 0);
			}
		}

		if (!overlapsWindow(oldWindow))
			oldWindow.uproot(this, windowVisual);
	}

	public void setAtomWindow(final Atom atom) {
		TagsChange tagsChange = new TagsChange();
		if (!window) {
			window = true;
			tagsChange = tagsChange.add(new StateTag("windowed"));
		}
		if (windowAtom == null)
			tagsChange = tagsChange.remove(new StateTag("root_window"));
		final Visual oldWindow = windowAtom == null ? document.root.visual : windowAtom.visual;
		windowAtom = atom;
		final Visual windowVisual = atom.createVisual(this, null, ImmutableMap.of(), 0);
		if (!overlapsWindow(oldWindow))
			oldWindow.uproot(this, windowVisual);
		if (!tagsChange.add.isEmpty() || !tagsChange.remove.isEmpty())
			changeGlobalTags(tagsChange);
		idleLayBricksOutward();
	}

	public void clearSelection() {
		selection.clear(this);
		selection = null;
	}

	public void setSelection(final Selection selection) {
		final int localToken = ++selectToken;
		final Selection oldSelection = this.selection;
		this.selection = selection;

		if (oldSelection != null) {
			oldSelection.clear(this);
		}

		if (localToken != selectToken)
			return;

		ImmutableSet.copyOf(selectionListeners).forEach(l -> l.selectionChanged(this, selection));
		selectionTagsChanged();
	}

	public Style.Baked getStyle(final Set<Tag> tags) {
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
					context.foreground.children.get(0).children.isEmpty() ?
							null :
							context.foreground.children.get(0).children.get(0)
			) : hoverBrick;
		}

		@Override
		public boolean runImplementation() {
			if (at == null || at.parent == null) {
				return false;
			}
			if (point == null) {
				if (hover != null) {
					clearHover();
				}
				hoverBrick = null;
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

	public void windowClear() {
		windowClearNoLayBricks();
		idleLayBricksOutward();
	}

	public void windowClearNoLayBricks() {
		window = false;
		windowAtom = null;
		document.root.createVisual(this, null, ImmutableMap.of(), 0);
		changeGlobalTags(new TagsChange(ImmutableSet.of(),
				ImmutableSet.of(new StateTag("windowed"), new StateTag("root_window"))
		));
	}

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
				windowClear();
			}

			@Override
			public String getName() {
				return "window_clear";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				if (!window)
					return;
				if (windowAtom == null)
					return;
				final Atom atom = windowAtom;
				final Visual oldWindowVisual = windowAtom.visual;
				final Visual windowVisual;
				if (atom == document.root) {
					windowAtom = null;
					windowVisual = document.root.createVisual(context, null, ImmutableMap.of(), 0);
				} else {
					windowAtom = atom.parent.value().parent.atom();
					windowVisual = windowAtom.createVisual(context, null, ImmutableMap.of(), 0);
				}
				idleLayBricksOutward();
			}

			@Override
			public String getName() {
				return "window_up";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				if (!window)
					return;
				final List<VisualAtom> chain = new ArrayList<>();
				final VisualAtom stop = windowAtom.visual;
				if (selection.getVisual().parent() == null)
					return;
				VisualAtom at = selection.getVisual().parent().atomVisual();
				while (at != null) {
					if (at == stop)
						break;
					if (at.parent() == null)
						break;
					chain.add(at);
					at = at.parent().atomVisual();
				}
				if (chain.isEmpty())
					return;
				final Visual oldWindowVisual = windowAtom.visual;
				final VisualAtom windowVisual = last(chain);
				windowAtom = windowVisual.atom;
				last(chain).root(context, null, ImmutableMap.of(), 0);
				oldWindowVisual.uproot(context, windowVisual);
				idleLayBricksOutward();
			}

			@Override
			public String getName() {
				return "window_down";
			}
		}));
		this.syntax = syntax;
		this.document = document;
		this.display = display;
		display.setBackgroundColor(syntax.background);
		edge = display.edge(this);
		transverseEdge = display.transverseEdge(this);
		background = display.group();
		midground = display.group();
		this.foreground = new Wall(this);
		this.overlay = display.group();
		display.add(background);
		display.add(midground);
		display.add(foreground.visual);
		display.add(overlay);
		this.addIdle = addIdle;
		banner = new Banner(this);
		details = new Details(this);
		this.history = history;
		display.addConverseEdgeListener((oldValue, newValue) -> {
			edge = Math.max(0, newValue - document.syntax.pad.converseStart - document.syntax.pad.converseEnd);
			if (newValue < oldValue) {
				foreground.idleCompact(this);
			} else if (newValue > oldValue) {
				foreground.idleExpand(this);
			}
			converseEdgeListeners.forEach(listener -> listener.changed(this, oldValue, newValue));
		});
		display.addTransverseEdgeListener((
				(oldValue, newValue) -> {
					transverseEdge = Math.max(0,
							newValue - document.syntax.pad.transverseStart - document.syntax.pad.transverseEnd
					);
					scrollVisible();
					transverseEdgeListeners.forEach(listener -> listener.changed(this, oldValue, newValue));
				}
		));
		display.addHIDEventListener(hidEvent -> {
			keyIgnore = false;
			if (!keyListeners.stream().allMatch(l -> l.handleKey(this, hidEvent)))
				return;
			keyIgnore = true;
		});
		display.addTypingListener(text -> {
			if (keyIgnore) {
				keyIgnore = false;
				return;
			}
			if (text.isEmpty())
				return;
			selection.receiveText(this, text);
		});
		display.addMouseExitListener(() -> {
			if (hoverIdle != null) {
				hoverIdle.point = null;
			} else if (hover != null) {
				clearHover();
				hover = null;
				hoverBrick = null;
			}
		});
		display.addMouseMoveListener(vector -> {
			if (hoverIdle == null) {
				hoverIdle = new HoverIdle(this);
				addIdle.accept(hoverIdle);
			}
			hoverIdle.point = vector.add(new Vector(-syntax.pad.converseStart, scroll));
		});
		history.addListener(new History.Listener() {
			@Override
			public void applied(final Context context, final Change change) {
				if (hoverIdle != null) {
					hoverIdle.destroy();
				}
			}
		});
		foreground.addCornerstoneListener(this, new Wall.CornerstoneListener() {
			Brick cornerstone = null;
			private final Attachment selectionBrickAttachment = new Attachment() {
				@Override
				public void setTransverse(final Context context, final int transverse) {
					final int oldScrollStart = scrollStart;
					scrollStart = transverse;
					scrollEnd += scrollStart - oldScrollStart;
					scrollVisible();
				}

				@Override
				public void setTransverseSpan(final Context context, final int ascent, final int descent) {
					scrollEnd = scrollStart + ascent + descent;
					scrollVisible();
				}

				@Override
				public void destroy(final Context context) {
					cornerstone = null;
				}
			};

			@Override
			public void cornerstoneChanged(final Context context, final Brick brick) {
				if (cornerstone != null) {
					cornerstone.removeAttachment(context, selectionBrickAttachment);
				}
				this.cornerstone = brick;
				cornerstone.addAttachment(context, selectionBrickAttachment);
			}
		});
		foreground.addBeddingListener(this, new Wall.BeddingListener() {
			@Override
			public void beddingChanged(final Context context, final int beddingBefore, final int beddingAfter) {
				scrollStartBeddingBefore = beddingBefore;
				scrollStartBeddingAfter = beddingAfter;
				scrollVisible();
			}
		});
		if (!syntax.startWindowed)
			windowClearNoLayBricks();
		else {
			window = true;
			windowAtom = null;
			document.root.createVisual(this, null, ImmutableMap.of(), 0);
			changeGlobalTags(new TagsChange(ImmutableSet.of(new StateTag("windowed"), new StateTag("root_window")),
					ImmutableSet.of()
			));
		}
		modules = document.syntax.modules.stream().map(p -> p.initialize(this)).collect(Collectors.toList());
		document.rootArray.selectDown(this);
		idleLayBricksOutward();
	}

	private void scrollVisible() {
		final int minimum = scrollStart - scrollStartBeddingBefore - syntax.pad.transverseStart;
		final int maximum = scrollEnd + scrollStartBeddingAfter + syntax.pad.transverseEnd;
		final int maxDiff = maximum - transverseEdge - scroll;
		Integer newScroll = null;
		if (minimum < scroll) {
			newScroll = minimum;
		} else if (maxDiff > 0 && scroll + maxDiff < minimum) {
			newScroll = scroll + maxDiff;
		}
		if (newScroll != null) {
			foreground.visual.setPosition(this,
					new Vector(syntax.pad.converseStart, -newScroll),
					syntax.animateCoursePlacement
			);
			background.setPosition(this,
					new Vector(syntax.pad.converseStart, -newScroll),
					syntax.animateCoursePlacement
			);
			overlay.setPosition(this, new Vector(syntax.pad.converseStart, -newScroll), syntax.animateCoursePlacement);
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
