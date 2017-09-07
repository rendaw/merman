package com.zarbosoft.merman.editor.wall;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.visual.VisualLeaf;
import com.zarbosoft.merman.editor.visual.alignment.ConcensusAlignment;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.*;

public class Course {

	public int index;
	final Group visual;
	final Group brickVisual;
	public Wall parent;
	private IterationPlaceTask idlePlace;
	private IterationCompactTask idleCompact;
	private IterationExpandTask idleExpand;
	public int transverseStart = 0;
	public int ascent = 0;
	public int descent = 0;
	public List<Brick> children = new ArrayList<>();
	int lastExpandCheckConverse = 0;

	Course(final Context context, final int transverseStart) {
		visual = context.display.group();
		brickVisual = context.display.group();
		visual.add(0, brickVisual);
		this.transverseStart = transverseStart;
		visual.setTransverse(context, transverseStart);
	}

	public int transverseEdge(final Context context) {
		return transverseStart + ascent + descent;
	}

	void setTransverse(final Context context, final int transverse) {
		transverseStart = transverse;
		visual.setPosition(context,
				new com.zarbosoft.merman.editor.visual.Vector(0, transverseStart),
				context.syntax.animateCoursePlacement
		);
		ImmutableList
				.copyOf(children)
				.stream()
				.forEach(c -> ImmutableList
						.copyOf(c.getAttachments(context))
						.stream()
						.forEach(a -> a.setTransverse(context, transverseStart)));
	}

	void changed(final Context context, final int at) {
		final Brick brick = children.get(at);
		final Brick.Properties properties = brick.properties(context);
		if (at > 0 && properties.split) {
			breakCourse(context, at);
			return;
		} else if (at == 0 && !properties.split && this.index > 0) {
			joinPreviousCourse(context);
			return;
		}
		getIdlePlace(context);
		idlePlace.at(at);
		idlePlace.changed.add(brick);
	}

	private void joinPreviousCourse(final Context context) {
		visual.clear();
		final boolean resetCornerstone = parent.cornerstoneCourse == this;
		final Course previous = parent.children.get(this.index - 1);
		previous.add(context, previous.children.size(), children);
		destroyInner(context);
		if (resetCornerstone)
			parent.setCornerstone(context, parent.cornerstone, null, null);
	}

	Course breakCourse(final Context context, final int index) {
		if (index == 0)
			throw new AssertionError("Breaking course at 0.");
		boolean resetCornerstone = false;
		final Course next = new Course(context, transverseStart + transverseSpan());
		parent.add(context, this.index + 1, ImmutableList.of(next));
		if (index < children.size()) {
			final List<Brick> transplant = ImmutableList.copyOf(children.subList(index, children.size()));
			getIdlePlace(context);
			for (final Brick brick : transplant) {
				idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.properties(context).ascent);
				idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.properties(context).descent);
				idlePlace.changed.remove(brick);
				if (brick == parent.cornerstone)
					resetCornerstone = true;
			}
			children.subList(index, children.size()).clear();
			visual.remove(index, visual.size() - index);
			next.add(context, 0, transplant);
		}
		if (resetCornerstone)
			parent.setCornerstone(context, parent.cornerstone, null, null);
		return next;
	}

	void add(final Context context, final int at, final List<Brick> bricks) {
		if (bricks.size() == 0)
			throw new AssertionError("Adding no bricks");
		children.addAll(at, bricks);
		for (int i = 0; i < bricks.size(); ++i) {
			final Brick brick = bricks.get(i);
			brick.setParent(context, this, at + i);
		}
		renumber(at + bricks.size());
		visual.addAll(at, bricks.stream().map(c -> c.getDisplayNode()).collect(Collectors.toList()));
		bricks.stream().forEach(c -> {
			c.allocateTransverse(context, ascent, descent);
			ImmutableList.copyOf(c.getAttachments(context)).stream().forEach(a -> {
				a.setTransverse(context, transverseStart);
				a.setTransverseSpan(context, ascent, descent);
			});
		});
		getIdlePlace(context);
		idlePlace.at(at);
		idlePlace.changed.addAll(bricks);
	}

	void removeFromSystem(final Context context, final int at) {
		final Brick brick = children.get(at);
		if (parent.cornerstone == brick)
			parent.cornerstone = null;
		if (context.hoverBrick == brick) {
			context.clearHover();
		}
		brick.setParent(context, null, 0);
		children.remove(at);
		if (children.isEmpty()) {
			if (index - 1 >= 0)
				parent.children.get(index - 1).getIdleExpand(context);
			if (index + 1 < parent.children.size())
				parent.children.get(index + 1).getIdleExpand(context);
			destroyInner(context);
		} else {
			if (at == 0 && this.index > 0) {
				joinPreviousCourse(context);
			} else {
				visual.remove(at);
				renumber(at);
				getIdlePlace(context);
				idlePlace.at(at);
				idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.properties(context).ascent);
				idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.properties(context).descent);
				idlePlace.changed.remove(brick);
			}
		}
	}

	private void destroyInner(final Context context) {
		if (idlePlace != null)
			idlePlace.destroy();
		if (idleCompact != null)
			idleCompact.destroy();
		if (idleExpand != null)
			idleExpand.destroy();
		parent.remove(context, index);
	}

	void destroy(final Context context) {
		while (!children.isEmpty())
			last(children).destroy(context);
	}

	private void renumber(final int at) {
		for (int index = at; index < children.size(); ++index) {
			children.get(index).index = index;
		}
	}

	private void getIdlePlace(final Context context) {
		if (idlePlace == null) {
			idlePlace = new IterationPlaceTask(context);
			context.addIdle(idlePlace);
		}
	}

	private void getIdleCompact(final Context context) {
		if (idleCompact == null) {
			idleCompact = new IterationCompactTask(context);
			context.addIdle(idleCompact);
		}
	}

	private void getIdleExpand(final Context context) {
		if (idleExpand == null) {
			idleExpand = new IterationExpandTask(context);
			context.addIdle(idleExpand);
		}
	}

	public int transverseSpan() {
		return ascent + descent;
	}

	private static class PlaceData {
		final BrickAdvanceContext advanceContext;
		final int minConverse;
		final int converse;
		final boolean ignoreAlignment;
		final boolean disableAlignment;

		private PlaceData(
				final BrickAdvanceContext advanceContext,
				final int minConverse,
				final int converse,
				final boolean ignoreAlignment,
				final boolean disableAlignment
		) {
			this.advanceContext = advanceContext;
			this.minConverse = minConverse;
			this.converse = converse;
			this.ignoreAlignment = ignoreAlignment;
			this.disableAlignment = disableAlignment;
		}
	}

	private static class BrickAdvanceContext {
		final int converse;
		final PSet<ConcensusAlignment> seenAlignments;
		final PSet<ConcensusAlignment> seenAlignmentsTransitive;

		public BrickAdvanceContext(
				final int converse,
				final PSet<ConcensusAlignment> seenAlignments,
				final PSet<ConcensusAlignment> seenAlignmentsTransitive
		) {
			this.converse = converse;
			this.seenAlignments = seenAlignments;
			this.seenAlignmentsTransitive = seenAlignmentsTransitive;
		}

		public BrickAdvanceContext() {
			this(0, HashTreePSet.empty(), HashTreePSet.empty());
		}
	}

	private PlaceData brickAdvanceLogic(
			final Context context, final BrickAdvanceContext advanceContext, final Brick.Properties properties
	) {
		PSet<ConcensusAlignment> seenAlignments = advanceContext.seenAlignments;
		PSet<ConcensusAlignment> seenAlignmentsTransitive = advanceContext.seenAlignmentsTransitive;
		boolean disableAlignment = false;
		boolean ignoreAlignment = false;
		if (properties.alignment instanceof ConcensusAlignment) {
			final ConcensusAlignment concensusAlignment = (ConcensusAlignment) properties.alignment;
			if (advanceContext.seenAlignments.contains(concensusAlignment)) {
				// If the same alignment appears twice feedback is disabled so converse will be low
				ignoreAlignment = true;
			} else if (advanceContext.seenAlignmentsTransitive.contains(concensusAlignment)) {
				// No need to disable here
				disableAlignment = true;
			} else {
				seenAlignments = seenAlignments.plus(concensusAlignment);
				seenAlignmentsTransitive =
						seenAlignmentsTransitive.plus(concensusAlignment).plusAll(concensusAlignment.superior);
			}
		}
		final int minConverse;
		final int converse;
		if (properties.alignment != null && !disableAlignment && !ignoreAlignment) {
			minConverse = advanceContext.converse;
			converse = Math.max(advanceContext.converse, properties.alignment.converse);
		} else {
			minConverse = 0;
			converse = advanceContext.converse;
		}
		return new PlaceData(
				new BrickAdvanceContext(converse + properties.converseSpan, seenAlignments, seenAlignmentsTransitive),
				minConverse,
				converse,
				ignoreAlignment,
				disableAlignment
		);
	}

	class IterationPlaceTask extends IterationTask {

		private final Context context;
		int first = Integer.MAX_VALUE;
		Set<Brick> changed = new HashSet<>();
		int removeMaxAscent = 0;
		int removeMaxDescent = 0;

		public IterationPlaceTask(final Context context) {
			this.context = context;
		}

		@Override
		protected double priority() {
			return P.coursePlace;
		}

		@Override
		public boolean runImplementation(final IterationContext iterationContext) {
			// Update transverse space
			boolean newAscent = false, newDescent = false;
			for (final Brick brick : changed) {
				final Brick.Properties properties = brick.properties(context);
				if (properties.ascent > ascent) {
					ascent = properties.ascent;
					newAscent = true;
				}
				if (properties.descent > descent) {
					descent = properties.descent;
					newDescent = true;
				}
			}
			if (!(newAscent && newDescent) && removeMaxAscent == ascent && removeMaxDescent == descent) {
				ascent = 0;
				descent = 0;
				{
					for (final Brick brick : children) {
						final Brick.Properties properties = brick.properties(context);
						ascent = Math.max(ascent, properties.ascent);
						descent = Math.max(descent, properties.descent);
					}
				}
				newAscent = true;
				newDescent = true;
			}
			if (newAscent || newDescent) {
				children.stream().forEach(b -> {
					b.allocateTransverse(context, ascent, descent);
					b.getAttachments(context).forEach(a -> a.setTransverseSpan(context, ascent, descent));
				});
			} else
				changed.stream().forEach(b -> {
					b.allocateTransverse(context, ascent, descent);
					b.getAttachments(context).forEach(a -> a.setTransverseSpan(context, ascent, descent));
				});

			// Do converse placement
			final int at = first;
			BrickAdvanceContext advanceContext = new BrickAdvanceContext(
					at == 0 ? 0 : (at >= children.size() ? last(children) : children.get(at - 1)).converseEdge(context),
					HashTreePSet.empty(),
					HashTreePSet.empty()
			);
			for (int index = 0; index < at && index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final Brick.Properties properties = brick.properties(context);
				if (properties.alignment == null || !(properties.alignment instanceof ConcensusAlignment))
					continue;
				advanceContext = new BrickAdvanceContext(advanceContext.converse,
						advanceContext.seenAlignments.plus((ConcensusAlignment) properties.alignment),
						advanceContext.seenAlignmentsTransitive
								.plus((ConcensusAlignment) properties.alignment)
								.plusAll(((ConcensusAlignment) properties.alignment).superior)
				);
			}
			for (int index = at; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final Brick.Properties properties = brick.properties(context);
				final PlaceData result = brickAdvanceLogic(context, advanceContext, properties);
				advanceContext = result.advanceContext;
				brick.setConverse(context, result.minConverse, result.converse);
				if (result.disableAlignment) {
					((ConcensusAlignment) properties.alignment).disable(context);
				} else if (result.ignoreAlignment) {
				} else if (properties.alignment instanceof ConcensusAlignment) {
					final ConcensusAlignment concensusAlignment = (ConcensusAlignment) properties.alignment;
					concensusAlignment.feedback(context, result.minConverse);
					concensusAlignment.superior.addAll(advanceContext.seenAlignmentsTransitive);
				}
				for (final Attachment attachment : brick.getAttachments(context))
					attachment.setConverse(context, result.converse);
			}
			if (advanceContext.converse > context.edge)
				getIdleCompact(context);
			if (advanceContext.converse * context.syntax.retryExpandFactor < lastExpandCheckConverse)
				getIdleExpand(context);
			if (advanceContext.converse > lastExpandCheckConverse)
				lastExpandCheckConverse = advanceContext.converse;

			// Propagate changes up
			if (newAscent || newDescent)
				parent.adjust(context, index);

			return false;
		}

		@Override
		protected void destroyed() {
			if (idlePlace == this)
				idlePlace = null;
		}

		public void at(final int at) {
			if (at < first)
				first = at;
		}
	}

	private final static Comparator<VisualAtom> compactComparator = new ChainComparator<VisualAtom>()
			.greaterFirst(VisualAtom::spacePriority)
			.lesserFirst(a -> a.depthScore)
			.build();
	private final static Comparator<VisualAtom> expandComparator = compactComparator.reversed();

	class IterationCompactTask extends IterationTask {
		private final Context context;
		private final Set<VisualAtom> skip = new HashSet<>();

		IterationCompactTask(final Context context) {
			this.context = context;
		}

		@Override
		protected double priority() {
			return P.courseCompact;
		}

		@Override
		public boolean runImplementation(final IterationContext iterationContext) {
			final Set<Course> maskedCourses = new HashSet<>();
			final Set<Course> queuedCourses;
			final List<VisualAtom> compact = new ArrayList<>();

			// Find higest priority brick in this course
			final PriorityQueue<VisualAtom> priorities = new PriorityQueue<>(11, compactComparator);
			int converse = 0;
			{
				for (int index = 0; index < children.size(); ++index) {
					final Brick brick = children.get(index);
					final VisualLeaf visual = brick.getVisual();
					final VisualAtom atomVisual = visual.parent().atomVisual();
					if (skip.contains(atomVisual))
						continue;
					if (!visual.atomVisual().compact || visual instanceof VisualPrimitive)
						priorities.add(atomVisual);
					converse = brick.converseEdge(context);
					if (!priorities.isEmpty() && converse > context.edge)
						break;
				}
			}
			if (converse <= context.edge) {
				return false;
			}
			if (priorities.isEmpty()) {
				return false;
			}
			final VisualAtom top = priorities.poll();
			top.compact(context);
			skip.add(top);
			return true;
		}

		@Override
		protected void destroyed() {
			idleCompact = null;
		}
	}

	private static class ExpandCalculateContext {
		public int index = -1;
		public BrickAdvanceContext advanceContext = new BrickAdvanceContext();
		public final Map<Brick, Brick.Properties> lookup;

		private ExpandCalculateContext(final Map<Brick, Brick.Properties> lookup) {
			this.lookup = lookup;
		}
	}

	class IterationExpandTask extends IterationTask {
		private final Context context;

		IterationExpandTask(final Context context) {
			this.context = context;
		}

		@Override
		protected double priority() {
			return P.courseExpand;
		}

		@Override
		public boolean runImplementation(final IterationContext iterationContext) {
			return expand(context, iterationContext);
		}

		private boolean calculateCourseConverse(final ExpandCalculateContext state, final Course course) {
			if (course.index <= state.index)
				return true;
			state.index = course.index;
			for (final Brick at : course.children) {
				final Brick.Properties properties;
				if (state.lookup.containsKey(at)) {
					properties = state.lookup.get(at);
				} else
					properties = at.properties(context);
				if (properties.split)
					state.advanceContext = new BrickAdvanceContext();
				state.advanceContext = brickAdvanceLogic(context, state.advanceContext, properties).advanceContext;
				if (state.advanceContext.converse > context.edge)
					return false;
			}
			return true;
		}

		private boolean expand(final Context context, final IterationContext iterationContext) {
			final PriorityQueue<VisualAtom> priorities = new PriorityQueue<>(11, expandComparator);

			// Find next atom that can be expanded
			for (int index = 0; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final VisualLeaf visual = brick.getVisual();
				if (visual.atomVisual().compact)
					priorities.add(visual.parent().atomVisual());
			}
			if (priorities.isEmpty())
				return false;
			final VisualAtom top = priorities.poll();

			// Check that all parents are either expanded or have lower expand priority
			{
				VisualAtom parentAtom = top;
				while (parentAtom.parent() != null) {
					parentAtom = parentAtom.parent().atomVisual();
					if (isOrdered(expandComparator, parentAtom, top) && parentAtom.compact)
						return false;
				}
			}

			// Check that all children are either expanded or have lower expand priority
			{
				final Brick first = top.getFirstBrick(context);
				final Brick last = top.getLastBrick(context);
				if (parent
						.streamRange(first.parent.index, first.index, last.parent.index, last.index)
						.anyMatch(brick -> {
							final VisualLeaf visual = brick.getVisual();
							final VisualAtom atom = visual.atomVisual();
							if (!atom.compact)
								return false;
							if (atom == top)
								return false;
							if (isOrdered(expandComparator, top, atom))
								return false;
							return true;
						}))
					return false;
			}

			// Check if we actually can expand
			{
				final Iterable<Pair<Brick, Brick.Properties>> brickProperties = top.getLeafPropertiesForTagsChange(
						context,
						new TagsChange(ImmutableSet.of(), ImmutableSet.of(new StateTag("compact")))
				);
				final Map<Brick, Brick.Properties> lookup =
						stream(brickProperties.iterator()).collect(Collectors.toMap(p -> p.first, p -> p.second));
				final ExpandCalculateContext state = new ExpandCalculateContext(lookup);
				for (final Pair<Brick, Brick.Properties> pair : brickProperties) {
					final Brick brick = pair.first;
					final Course course = brick.parent;
					if (course.index > 0 && brick.index == 0 && !pair.second.split)
						if (!calculateCourseConverse(state, parent.children.get(course.index - 1)))
							return false;
					if (!calculateCourseConverse(state, course))
						return false;
				}
			}

			// Avoid bouncing
			if (iterationContext.expanded.contains(top))
				return false;
			iterationContext.expanded.add(top);

			// Expand
			top.expand(context);
			lastExpandCheckConverse = 0;
			return true;
		}

		@Override
		protected void destroyed() {
			idleExpand = null;
		}
	}
}
