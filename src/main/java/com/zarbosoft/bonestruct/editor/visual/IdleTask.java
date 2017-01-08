package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.ChainComparator;

import java.util.Comparator;

public abstract class IdleTask implements Comparable<IdleTask> {
	private static final Comparator<IdleTask> comparator =
			new ChainComparator<IdleTask>().greaterFirst(t -> t.priority()).build();

	protected int priority() {
		return 0;
	}

	public abstract void run();

	@Override
	public int compareTo(final IdleTask t) {
		return comparator.compare(this, t);
	}
}
