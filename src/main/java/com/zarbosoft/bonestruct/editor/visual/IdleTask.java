package com.zarbosoft.bonestruct.editor.visual;

public abstract class IdleTask implements Comparable<IdleTask> {
	protected int priority() {
		return 0;
	}

	public abstract void run();

	@Override
	public int compareTo(final IdleTask t) {
		return priority() - t.priority();
	}
}
