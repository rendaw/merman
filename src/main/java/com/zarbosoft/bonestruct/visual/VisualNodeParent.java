package com.zarbosoft.bonestruct.visual;

public abstract class VisualNodeParent {
	public abstract VisualNodeParent parent();

	public abstract void updatePriority(final int treeCompactPriority);
}
