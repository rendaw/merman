package com.zarbosoft.merman.editor.visual.alignment;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IdleTask;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.AlignmentListener;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.Course;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConcensusAlignment extends Alignment {
	/**
	 * Only bricks affect concensus.
	 */
	private final Map<Course, Set<Brick>> courseCounts = new HashMap<>();

	private IdleFeedback idleFeedback;

	private class IdleFeedback extends IdleTask {
		private final Context context;

		private IdleFeedback(final Context context) {
			this.context = context;
		}

		@Override
		protected boolean runImplementation() {
			final int oldConverse = converse;
			converse = courseCounts
					.entrySet()
					.stream()
					.filter(entry -> entry.getValue().size() == 1)
					.mapToInt(entry -> entry.getValue().stream().mapToInt(brick -> brick.minConverse).max().orElse(0))
					.max()
					.orElse(0);
			if (oldConverse != converse) {
				courseCounts
						.entrySet()
						.stream()
						.filter(entry -> entry.getValue().size() == 1)
						.forEach(entry -> entry.getValue().stream().forEach(brick -> brick.align(context)));
				listeners
						.stream()
						.filter(listener -> (listener instanceof Brick))
						.forEach(listener -> listener.align(context));
			}
			return false;
		}

		@Override
		protected void destroyed() {
			idleFeedback = null;
		}
	}

	private void idleFeedback(final Context context) {
		if (idleFeedback == null) {
			idleFeedback = new IdleFeedback(context);
			context.addIdle(idleFeedback);
		}
	}

	@Override
	public void destroy(final Context context) {
		if (idleFeedback != null)
			idleFeedback.destroy();
	}

	@Override
	public void feedback(final Context context, final int gotConverse) {
		idleFeedback(context);
	}

	@Override
	public void addListener(final Context context, final AlignmentListener listener) {
		super.addListener(context, listener);
		if (listener instanceof Brick) {
			addedToCourse(context, (Brick) listener);
		}
	}

	@Override
	public void removeListener(
			final Context context, final AlignmentListener listener
	) {
		super.removeListener(context, listener);
		if (listener instanceof Brick) {
			removedFromCourse(context, (Brick) listener, ((Brick) listener).parent);
		}
	}

	private void addedToCourse(final Context context, final Brick brick) {
		final Set<Brick> set = courseCounts.computeIfAbsent(brick.parent, k -> new HashSet<>());
		set.add(brick);
		if (set.isEmpty())
			courseCounts.remove(brick.parent);
		else if (set.size() == 1 && brick.minConverse > converse)
			idleFeedback(context);
	}

	private void removedFromCourse(final Context context, final Brick brick, final Course parent) {
		final Set<Brick> set = courseCounts.get(parent);
		if (set == null) // courseChange on new bricks, not yet added
			return;
		set.remove(brick);
		if (brick.minConverse == converse)
			idleFeedback(context);
	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
	}

	@Override
	public void courseChanged(final Context context, final Brick brick, final Course oldParent) {
		removedFromCourse(context, brick, oldParent);
		addedToCourse(context, brick);
	}

	@Override
	public boolean enabledForCourse(final Course parent) {
		return courseCounts.getOrDefault(parent, ImmutableSet.of()).size() == 1;
	}
}
