package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.rendaw.common.ChainComparator;

import java.util.Comparator;

public abstract class IdleTask implements Comparable<IdleTask> {
	private static final Comparator<IdleTask> comparator =
			new ChainComparator<IdleTask>().greaterFirst(t -> t.priority()).build();
	public boolean destroyed = false;

	protected double priority() {
		return 0;
	}

	protected abstract boolean runImplementation();

	public boolean run() {
		if (destroyed)
			return false;
		final boolean out = runImplementation();
		if (!out)
			destroy();
		return out;
	}

	@Override
	public int compareTo(final IdleTask t) {
		return comparator.compare(this, t);
	}

	public void destroy() {
		if (destroyed)
			throw new AssertionError();
		destroyed();
		destroyed = true;
	}

	protected abstract void destroyed();

}
