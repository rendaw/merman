package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;

import java.util.PriorityQueue;

public class IdleRunner {
	private final PriorityQueue<IterationTask> idleQueue = new PriorityQueue<>();

	public void idleAdd(final IterationTask task) {
		idleQueue.add(task);
	}

	public void flush() {
		final IterationContext iterationContext = new IterationContext();
		for (int i = 0; i < 1000; ++i) { // Batch
			final IterationTask top = idleQueue.poll();
			if (top == null) {
				return;
			} else {
				if (top.run(iterationContext))
					idleAdd(top);
			}
		}
		throw new AssertionError("Too much idle activity");
	}
}
