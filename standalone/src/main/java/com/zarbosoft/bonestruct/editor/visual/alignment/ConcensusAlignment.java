package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Course;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConcensusAlignment extends Alignment {
	private final Map<Course, Set<Brick>> courseCounts = new HashMap<>();

	@Override
	public void feedback(final Context context, final int gotConverse) {
		final int oldConverse = this.converse;
		if (gotConverse == this.converse)
			return;
		if (gotConverse > this.converse) {
			this.converse = gotConverse;
		} else {
			reduce(context);
		}
		if (this.converse != oldConverse)
			submit(context);
	}

	@Override
	public void addListener(final Context context, final AlignmentListener listener) {
		super.addListener(context, listener);
		if (listener instanceof Brick) {
			addedToCourse(context, (Brick) listener, ((Brick) listener).parent);
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
		removeVote(context, listener);
	}

	private void addedToCourse(final Context context, final Brick brick, final Course course) {
		final Set<Brick> set = courseCounts.computeIfAbsent(brick.parent, k -> new HashSet<>());
		set.add(brick);
		if (set.size() == 1) {
		} else if (set.size() == 2)
			set.stream().forEach(brick1 -> removeVote(context, brick1));
		else {
		}
	}

	private void removedFromCourse(final Context context, final Brick brick, final Course parent) {
		final Set<Brick> set = courseCounts.get(brick.parent);
		if (set == null)
			return;
		set.remove(brick);
		if (set.size() == 1)
			set.stream().forEach(brick1 -> {
				feedback(context, brick.minConverse);
			});
	}

	private void removeVote(final Context context, final AlignmentListener listener) {
		if (listener.getMinConverse(context) == converse) {
			final int oldConverse = converse;
			reduce(context);
			if (converse != oldConverse)
				submit(context);
		}
	}

	private void reduce(final Context context) {
		converse = 0;
		for (final AlignmentListener listener : listeners) {
			converse = Math.max(listener.getMinConverse(context), converse);
		}
	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
	}

	@Override
	public void courseChanged(final Context context, final Brick brick, final Course parent, final Course newParent) {
		removedFromCourse(context, brick, parent);
		addedToCourse(context, brick, newParent);
	}

	@Override
	public boolean enabledForCourse(final Course parent) {
		return courseCounts.getOrDefault(parent, ImmutableSet.of()).size() == 1;
	}
}
