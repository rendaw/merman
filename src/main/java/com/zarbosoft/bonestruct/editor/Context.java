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
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtomType;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Wall;
import com.zarbosoft.bonestruct.modules.Module;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
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

import static com.zarbosoft.rendaw.common.Common.last;

public class Context {
	public final History history;
	WeakCache<Set<Visual.Tag>, Style.Baked> styleCache = new WeakCache<>(v -> v.tags);
	public boolean window;
	public Atom windowAtom;
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
	public Banner banner;
	public Details details;
	public final Display display;
	int scrollStart;
	int scrollEnd;
	int scrollStartBeddingBefore;
	int scrollStartBeddingAfter;
	public int scroll;
	int selectToken = 0;

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
			return;
		if (index > 0) {
			final Brick previousBrick = accessLast.apply(index - 1);
			if (previousBrick == null)
				return;
			if (index + addCount < size) {
				// Hits neither edge
				final Brick nextBrick = accessFirst.apply(index + addCount);
				if (nextBrick != null) {
					fillFromStartBrick(previousBrick);
				}
			} else {
				// Hits end edge
				final boolean nextEdge = parent == null ? true : parent.isNextWindowEdge(this);
				if (!nextEdge && parent.getNextBrick(this) == null)
					return;
				fillFromStartBrick(previousBrick);
			}
		} else {
			final boolean previousEdge = parent == null ? true : parent.isPreviousWindowEdge(this);
			final Brick previousBrick = parent == null ? null : parent.getPreviousBrick(this);
			if (index + addCount < size) {
				// Hits index edge
				final Brick nextBrick = accessFirst.apply(index + addCount);
				if (nextBrick != null) {
					if (previousEdge) {
						fillFromEndBrick(nextBrick);
					} else if (previousBrick != null) {
						fillFromStartBrick(previousBrick);
					}
				}
			} else {
				// Hits both edges
				final boolean nextEdge = parent == null ? true : parent.isNextWindowEdge(this);
				if (previousEdge && nextEdge) {
					foreground.setCornerstone(this, create.apply(index));
				} else {
					final Brick nextBrick = parent.getNextBrick(this);
					if (previousEdge && nextBrick != null) {
						fillFromEndBrick(nextBrick);
					} else if (nextEdge && previousBrick != null) {
						fillFromStartBrick(previousBrick);
					} else if (previousBrick != null && nextBrick != null) {
						fillFromStartBrick(previousBrick);
					}
				}
			}
		}
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
		// Either (value) or (atom & part) are always set
		Value value = document.top;
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

	public void fillFromStartBrick(final Brick start) {
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

	private boolean overlapsWindow(final Visual visual) {
		Visual at = visual;
		while (true) {
			if (at == windowAtom.visual)
				return true;
			if (at.parent() == null)
				break;
			at = at.parent().getTarget();
		}
		return false;
	}

	public void createWindowForSelection(final Value value, final int depthThreshold) {
		final Visual oldWindow = windowAtom == null ? document.top.visual : windowAtom.visual;

		windowAtom = value.parent.node();
		int depth = 0;
		while (true) {
			depth += windowAtom.type.depthScore;
			if (depth > depthThreshold)
				break;
			if (windowAtom.parent.value().parent == null)
				break;
			windowAtom = windowAtom.parent.value().parent.node();
		}

		if (depth <= depthThreshold) {
			windowAtom = null;
			final Atom rootAtom = new Atom(ImmutableMap.of("value", document.top));
			final Visual windowVisual =
					syntax.rootFront.createVisual(this, null, rootAtom, HashTreePSet.empty(), ImmutableMap.of(), 0);
		} else {
			final Visual windowVisual = windowAtom.createVisual(this, null, ImmutableMap.of(), 0);
		}

		if (!overlapsWindow(oldWindow))
			oldWindow.uproot(this, null);
	}

	public void setAtomWindow(final Atom atom) {
		Visual.TagsChange tagsChange = new Visual.TagsChange();
		if (window) {
			if (windowAtom == null)
				tagsChange = tagsChange.remove(new Visual.StateTag("root_window"));
		} else {
			window = true;
			tagsChange = tagsChange.add(new Visual.StateTag("windowed")).remove(new Visual.StateTag("root_window"));
		}
		final Visual oldWindow = windowAtom == null ? document.top.visual : windowAtom.visual;
		windowAtom = atom;
		final Visual windowVisual = atom.createVisual(this, null, ImmutableMap.of(), 0);
		if (!overlapsWindow(oldWindow))
			oldWindow.uproot(this, windowVisual);
		if (!tagsChange.add.isEmpty() || !tagsChange.remove.isEmpty())
			changeGlobalTags(tagsChange);
		fillOutward();
	}

	private void fillOutward() {
		fillFromStartBrick(foreground.children.get(0).children.get(0));
		fillFromEndBrick(last(last(foreground.children).children));
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

		final VisualPart visual = this.selection.getVisual();
		Brick first = visual.getFirstBrick(this);
		if (first == null)
			first = visual.createFirstBrick(this);
		foreground.setCornerstone(this, first);
		fillFromStartBrick(first);
		fillFromEndBrick(first);

		selection.addBrickListener(this, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				foreground.setCornerstone(context, brick);
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {

			}
		});
		selection.addBrickListener(this, selectionExtentsAdapter.boundsListener);

		ImmutableSet.copyOf(selectionListeners).forEach(l -> l.selectionChanged(this, selection));
		selectionTagsChanged();
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

	public void windowClear() {
		windowClearNoFill();
		fillOutward();
	}

	public void windowClearNoFill() {
		window = false;
		windowAtom = null;
		syntax.rootFront.createVisual(this,
				null,
				new Atom(ImmutableMap.of("value", document.top)),
				HashTreePSet.empty(),
				ImmutableMap.of(),
				0
		);
		changeGlobalTags(new Visual.TagsChange(ImmutableSet.of(),
				ImmutableSet.of(new Visual.StateTag("windowed"), new Visual.StateTag("root_window"))
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
				if (atom.parent.value().parent == null) {
					final Atom rootAtom = new Atom(ImmutableMap.of("value", document.top));
					windowVisual = syntax.rootFront.createVisual(context,
							null,
							rootAtom,
							HashTreePSet.empty(),
							ImmutableMap.of(),
							0
					);
				} else {
					windowVisual = atom.parent.value().parent.node().createVisual(context, null, ImmutableMap.of(), 0);
				}
				fillOutward();
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
				final List<VisualAtomType> chain = new ArrayList<>();
				final VisualAtomType stop = windowAtom.visual;
				if (selection.getVisual().parent() == null)
					return;
				VisualAtomType at = selection.getVisual().parent().getNodeVisual();
				while (at != null) {
					if (at == stop)
						break;
					if (at.parent() == null)
						break;
					chain.add(at);
					at = at.parent().getNodeVisual();
				}
				if (chain.isEmpty())
					return;
				final Visual oldWindowVisual = windowAtom.visual;
				final VisualAtomType windowVisual = last(chain);
				windowAtom = windowVisual.atom;
				last(chain).root(context, null, ImmutableMap.of(), 0);
				oldWindowVisual.uproot(context, windowVisual);
				fillOutward();
			}

			@Override
			public String getName() {
				return "window_down";
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
		if (!syntax.startWindowed)
			windowClearNoFill();
		document.top.selectDown(this);
		fillOutward();
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
