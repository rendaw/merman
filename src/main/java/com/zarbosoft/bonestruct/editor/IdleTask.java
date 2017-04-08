package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.rendaw.common.ChainComparator;

import java.util.Comparator;

public abstract class IdleTask implements Comparable<IdleTask> {
	private static final Comparator<IdleTask> comparator =
			new ChainComparator<IdleTask>().greaterFirst(t -> t.priority()).build();
	private boolean destroyed = false;

	protected int priority() {
		return 0;
	}

	protected abstract void runImplementation();

	public void run() {
		if (destroyed)
			return;
		runImplementation();
	}

	@Override
	public int compareTo(final IdleTask t) {
		return comparator.compare(this, t);
	}

	public void destroy() {
		destroyed();
		destroyed = true;
	}

	protected abstract void destroyed();

}
