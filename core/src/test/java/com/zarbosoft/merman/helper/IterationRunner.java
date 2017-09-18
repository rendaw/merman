package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;

import java.util.PriorityQueue;

public class IterationRunner {
	private final PriorityQueue<IterationTask> idleQueue = new PriorityQueue<>();

	public void addIteration(final IterationTask task) {
		idleQueue.add(task);
	}

	public boolean flushInner(final int limit) {
		final IterationContext iterationContext = new IterationContext();
		for (int i = 0; i < limit; ++i) { // Batch
			final IterationTask top = idleQueue.poll();
			if (top == null) {
				return true;
			} else {
				if (top.run(iterationContext))
					addIteration(top);
			}
		}
		return false;
	}

	public void flushIteration(final int limit) {
		flushInner(limit);
	}

	public void flush() {
		if (!flushInner(1000))
			throw new AssertionError("Too much idle activity");
	}
}
