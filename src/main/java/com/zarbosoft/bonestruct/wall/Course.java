package com.zarbosoft.bonestruct.wall;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.display.Group;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;
import static com.zarbosoft.rendaw.common.Common.stream;

public class Course {

	public int index;
	final Group visual;
	final Group brickVisual;
	Wall parent;
	private IdlePlaceTask idlePlace;
	private IdleCompactTask idleCompact;
	private IdleExpandTask idleExpand;
	public int transverseStart = 0;
	int ascent = 0;
	int descent = 0;
	public List<Brick> children = new ArrayList<>();
	int lastExpandCheckConverse = 0;
	public int beddingBefore = 0;
	int beddingAfter = 0;

	Course(final Context context) {
		visual = context.display.group();
		brickVisual = context.display.group();
		visual.add(0, brickVisual);
	}

	public int transverseEdge(final Context context) {
		final int out = transverseStart + ascent + descent + beddingAfter;
		return out;
	}

	void setTransverse(final Context context, final int transverse) {
		transverseStart = transverse + beddingBefore;
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
		if (at > 0 && properties.broken) {
			breakCourse(context, at);
			return;
		} else if (at == 0 && !properties.broken && this.index > 0) {
			joinPreviousCourse(context);
			return;
		}
		getIdlePlace(context);
		idlePlace.at(at);
		idlePlace.changed.add(brick);
	}

	private void joinPreviousCourse(final Context context) {
		visual.clear();
		final Course previous = parent.children.get(this.index - 1);
		previous.add(context, previous.children.size(), children);
		destroyInner(context);
	}

	Course breakCourse(final Context context, final int index) {
		if (index == 0)
			throw new AssertionError("Breaking course at 0.");
		final Course next = new Course(context);
		parent.add(context, this.index + 1, ImmutableList.of(next));
		if (index < children.size()) {
			final List<Brick> transplant = ImmutableList.copyOf(children.subList(index, children.size()));
			boolean beddingChanged = false;
			getIdlePlace(context);
			for (final Brick brick : transplant) {
				idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.properties(context).ascent);
				idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.properties(context).descent);
				idlePlace.changed.remove(brick);
				if (!brick.getBeddings(context).isEmpty())
					beddingChanged = true;
			}
			children.subList(index, children.size()).clear();
			visual.remove(index, visual.size() - index);
			next.add(context, 0, transplant);
			if (beddingChanged)
				beddingChanged(context);
		}
		return next;
	}

	void add(final Context context, final int at, final List<Brick> bricks) {
		if (bricks.size() == 0)
			throw new AssertionError("Adding no bricks");
		children.addAll(at, bricks);
		boolean beddingChanged = false;
		for (final Brick brick : bricks) {
			brick.parent = this;
			if (!brick.getBeddings(context).isEmpty())
				beddingChanged = true;
		}
		renumber(at);
		visual.addAll(at, bricks.stream().map(c -> c.getRawVisual()).collect(Collectors.toList()));
		getIdlePlace(context);
		idlePlace.at(at);
		idlePlace.changed.addAll(bricks);
		if (beddingChanged)
			beddingChanged(context);
	}

	void removeFromSystem(final Context context, final int at) {
		final Brick brick = children.get(at);
		children.remove(at);
		if (children.isEmpty()) {
			destroyInner(context);
		} else {
			if (at == 0) {
				joinPreviousCourse(context);
			} else {
				if (!brick.getBeddings(context).isEmpty())
					beddingChanged(context);
				visual.remove(at);
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

	public void beddingChanged(final Context context) {
		final Pair<Integer, Integer> pair = ImmutableList
				.copyOf(children)
				.stream()
				.map(c -> ImmutableList
						.copyOf(c.getBeddings(context))
						.stream()
						.map(b -> new Pair<>(b.before, b.after))
						.reduce((a, b) -> new Pair<>(a.first + b.first, a.second + b.second))
						.orElse(new Pair<>(0, 0)))
				.reduce((a, b) -> new Pair<>(a.first + b.first, a.second + b.second))
				.orElse(new Pair<>(0, 0));
		beddingBefore = pair.first;
		beddingAfter = pair.second;
		ImmutableList
				.copyOf(children)
				.stream()
				.forEach(c -> ImmutableList
						.copyOf(c.getBeddingListeners())
						.stream()
						.forEach(a -> a.beddingChanged(context, beddingBefore, beddingAfter)));
		parent.adjust(context, this.index);
	}

	public int transverseSpan() {
		return beddingBefore + ascent + descent + beddingAfter;
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
			final Set<Alignment> seenAlignments = new HashSet<>();
			final int at = first;
			int converse =
					at == 0 ? 0 : (at >= children.size() ? last(children) : children.get(at - 1)).converseEdge(context);
			for (int index = at; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final Brick.Properties properties = brick.properties(context);
				final int minConverse = converse;
				if (properties.alignment != null && !seenAlignments.contains(properties.alignment)) {
					seenAlignments.add(properties.alignment);
					converse = Math.max(converse, properties.alignment.converse);
					properties.alignment.set(context, converse);
				}
				brick.setConverse(context, minConverse, converse);
				for (final Attachment attachment : brick.getAttachments(context))
					attachment.setConverse(context, converse);
				converse = brick.converseEdge(context);
				if (converse > context.edge)
					getIdleCompact(context);
			}
			if (converse < lastExpandCheckConverse)
				getIdleExpand(context);

			// Propagate changes up
			if (newAscent || newDescent/* || fixtures[0] || fixtures[1]*/)
				parent.adjust(context, index);

			idlePlace = null;

			return false;
		}

		@Override
		protected void destroyed() {
			idlePlace = null;
		}

		public void at(final int at) {
			if (at < first)
				first = at;
		}
	}

	boolean compact(final Context context) {
		final PriorityQueue<Visual> priorities =
				new PriorityQueue<>(11, new ChainComparator<Visual>().greaterFirst(Visual::spacePriority).build());
		int converse = 0;
		for (int index = 0; index < children.size(); ++index) {
			final Brick brick = children.get(index);
			final Visual visual = brick.getVisual();
			if (visual.canCompact())
				priorities.add(visual);
			converse = brick.converseEdge(context);
			if (!priorities.isEmpty() && converse > context.edge)
				break;
		}
		if (converse <= context.edge) {
			lastExpandCheckConverse = converse;
			return false;
		}
		if (priorities.isEmpty()) {
			return false;
		}
		priorities.poll().compact(context);
		return true;
	}

	class IdleCompactTask extends IdleTask {
		private final Context context;

		IdleCompactTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 150;
		}

		@Override
		public boolean runImplementation() {
			if (compact(context)) {
				return true;
			} else {
				idleCompact = null;
				return false;
			}
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
			if (expand(context)) {
				return true;
			} else {
				idleExpand = null;
				return false;
			}
		}

		private boolean expand(final Context context) {
			final PriorityQueue<Visual> priorities =
					new PriorityQueue<>(11, new ChainComparator<Visual>().lesserFirst(Visual::spacePriority).build());
			for (int index = 0; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final Visual visual = brick.getVisual();
				if (visual.canExpand())
					priorities.add(visual);
			}
			if (priorities.isEmpty())
				return false;
			final Visual top = priorities.poll();
			if (top == lastTarget)
				return false;
			lastTarget = top;
			final Iterable<Pair<Brick, Brick.Properties>> brickProperties = top.getPropertiesForTagsChange(context,
					new Visual.TagsChange(ImmutableSet.of(new Visual.StateTag("expanded")),
							ImmutableSet.of(new Visual.StateTag("compact"))
					)
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
				} else if (brick.parent.index == course.index + 1 && !pair.second.broken && brick.index == 0) {
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
					if (properties.alignment != null) {
						converse = Math.max(converse, properties.alignment.converse);
					}
					converse += properties.converseSpan;
					if (converse >= context.edge)
						return false;
				}
			}
			top.expand(context);
			return true;
		}

		@Override
		protected void destroyed() {
			idleExpand = null;
		}
	}
}
