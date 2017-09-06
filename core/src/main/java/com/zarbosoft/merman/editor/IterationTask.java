package com.zarbosoft.merman.editor;

import com.zarbosoft.rendaw.common.ChainComparator;

import java.util.Comparator;

public abstract class IterationTask implements Comparable<IterationTask> {
	private static final Comparator<IterationTask> comparator =
			new ChainComparator<IterationTask>().greaterFirst(t -> t.priority()).build();
	public boolean destroyed = false;

	protected double priority() {
		return 0;
	}

	protected abstract boolean runImplementation(IterationContext iterationContext);

	public boolean run(final IterationContext iterationContext) {
		if (destroyed)
			return false;
		final boolean out = runImplementation(iterationContext);
		if (!out)
			destroy();
		return out;
	}

	@Override
	public int compareTo(final IterationTask t) {
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
