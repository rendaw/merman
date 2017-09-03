package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.editor.IdleTask;

import java.util.PriorityQueue;

public class IdleRunner {
	private final PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();

	public void idleAdd(final IdleTask task) {
		idleQueue.add(task);
	}

	public void flush() {
		for (int i = 0; i < 1000; ++i) { // Batch
			final IdleTask top = idleQueue.poll();
			if (top == null) {
				return;
			} else {
				if (top.run())
					idleAdd(top);
			}
		}
		throw new AssertionError("Too much idle activity");
	}
}
