package com.zarbosoft.bonestruct.editor.wall;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.display.Group;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualLeaf;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.*;

public class Course {

	public int index;
	final Group visual;
	final Group brickVisual;
	Wall parent;
	private IdlePlaceTask idlePlace;
	private IdleCompactTask idleCompact;
	private IdleExpandTask idleExpand;
	public int transverseStart = 0;
	public int ascent = 0;
	public int descent = 0;
	public List<Brick> children = new ArrayList<>();
	int lastExpandCheckConverse = 0;

	Course(final Context context) {
		visual = context.display.group();
		brickVisual = context.display.group();
		visual.add(0, brickVisual);
	}

	public int transverseEdge(final Context context) {
		return transverseStart + ascent + descent;
	}

	void setTransverse(final Context context, final int transverse) {
		transverseStart = transverse;
		visual.setPosition(context,
				new com.zarbosoft.bonestruct.editor.visual.Vector(0, transverseStart),
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
			parent.setCornerstone(context, parent.cornerstone);
	}

	Course breakCourse(final Context context, final int index) {
		if (index == 0)
			throw new AssertionError("Breaking course at 0.");
		boolean resetCornerstone = false;
		final Course next = new Course(context);
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
		parent.setCornerstone(context, parent.cornerstone);
		return next;
	}

	void add(final Context context, final int at, final List<Brick> bricks) {
		if (bricks.size() == 0)
			throw new AssertionError("Adding no bricks");
		children.addAll(at, bricks);
		for (final Brick brick : bricks)
			brick.setParent(context, this);
		renumber(at);
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
		if (context.hoverBrick == brick) {
			context.clearHover();
		}
		brick.setParent(context, null);
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
			idlePlace = new IdlePlaceTask(context);
			context.addIdle(idlePlace);
		}
	}

	private void getIdleCompact(final Context context) {
		if (idleCompact == null) {
			idleCompact = new IdleCompactTask(context);
			context.addIdle(idleCompact);
		}
	}

	private void getIdleExpand(final Context context) {
		if (idleExpand == null) {
			idleExpand = new IdleExpandTask(context);
			context.addIdle(idleExpand);
		}
	}

	public int transverseSpan() {
		return ascent + descent;
	}

	private static class PlaceData {
		final int converse;
		final int minConverse;
		final int nextConverse;

		private PlaceData(final int converse, final int minConverse, final int nextConverse) {
			this.converse = converse;
			this.minConverse = minConverse;
			this.nextConverse = nextConverse;
		}
	}

	private PlaceData brickAdvanceLogic(
			final Context context, int converse, final Brick brick, final Brick.Properties properties
	) {
		final int minConverse;
		if (properties.alignment != null && properties.alignment.enabledForCourse(this)) {
			minConverse = converse;
			converse = Math.max(converse, properties.alignment.converse);
		} else
			minConverse = 0;
		return new PlaceData(converse, minConverse, converse + properties.converseSpan);
	}

	class IdlePlaceTask extends IdleTask {

		private final Context context;
		int first = Integer.MAX_VALUE;
		Set<Brick> changed = new HashSet<>();
		int removeMaxAscent = 0;
		int removeMaxDescent = 0;

		public IdlePlaceTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 170;
		}

		@Override
		public boolean runImplementation() {
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
			int converse =
					at == 0 ? 0 : (at >= children.size() ? last(children) : children.get(at - 1)).converseEdge(context);
			for (int index = at; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final Brick.Properties properties = brick.properties(context);
				final PlaceData result = brickAdvanceLogic(context, converse, brick, properties);
				brick.setConverse(context, result.minConverse, result.converse);
				if (properties.alignment != null && properties.alignment.enabledForCourse(Course.this)) {
					properties.alignment.feedback(context, result.minConverse);
				}
				for (final Attachment attachment : brick.getAttachments(context))
					attachment.setConverse(context, result.converse);
				converse = result.nextConverse;
			}
			if (converse > context.edge)
				getIdleCompact(context);
			if (converse * context.syntax.retryExpandFactor < lastExpandCheckConverse)
				getIdleExpand(context);
			if (converse > lastExpandCheckConverse)
				lastExpandCheckConverse = converse;

			// Propagate changes up
			if (newAscent || newDescent/* || fixtures[0] || fixtures[1]*/)
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

	private final static Comparator<VisualAtom> compactComparator =
			new ChainComparator<VisualAtom>().greaterFirst(VisualAtom::spacePriority).lesserFirst(a -> a.depth).build();
	private final static Comparator<VisualAtom> expandComparator = compactComparator.reversed();

	class IdleCompactTask extends IdleTask {
		private final Context context;
		private final Set<VisualAtom> skip = new HashSet<>();

		IdleCompactTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 150;
		}

		@Override
		public boolean runImplementation() {
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
					if (visual.canCompact())
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

	class IdleExpandTask extends IdleTask {
		private final Context context;
		private Visual lastTarget = null;

		IdleExpandTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return -95;
		}

		@Override
		public boolean runImplementation() {
			return expand(context);
		}

		private boolean expand(final Context context) {
			final PriorityQueue<VisualAtom> priorities = new PriorityQueue<>(11, expandComparator);

			// Find next atom that can be expanded
			for (int index = 0; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final VisualLeaf visual = brick.getVisual();
				if (visual.canExpand())
					priorities.add(visual.parent().atomVisual());
			}
			if (priorities.isEmpty())
				return false;
			final VisualAtom top = priorities.poll();
			if (top == lastTarget)
				return false;
			lastTarget = top;

			// Check that all parents are either expanded or have lower expand priority
			{
				VisualAtom parentAtom = top;
				while (parentAtom.parent() != null) {
					parentAtom = parentAtom.parent().atomVisual();
					if (parentAtom.compact && isOrdered(expandComparator, parentAtom, top))
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
							if (!visual.canExpand())
								return false;
							final VisualAtom atom = visual.parent().atomVisual();
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
				final Set<Brick> seen = new HashSet<>();
				int converse = 0;
				Course course = null;
				for (final Pair<Brick, Brick.Properties> pair : brickProperties) {
					final Brick brick = pair.first;
					if (seen.contains(brick))
						continue;
					if (course == null) {
					} else if (brick.parent.index == course.index + 1 && !pair.second.split && brick.index == 0) {
					} else {
						converse = 0;
					}
					course = brick.parent;
					for (final Brick at : course.children) {
						final Brick.Properties properties;
						if (lookup.containsKey(at)) {
							seen.add(at);
							properties = lookup.get(at);
						} else
							properties = at.properties(context);
						converse = brickAdvanceLogic(context, converse, brick, properties).nextConverse;
						if (converse > context.edge)
							return false;
					}
				}
			}

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
