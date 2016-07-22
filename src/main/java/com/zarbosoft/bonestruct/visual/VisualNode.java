package com.zarbosoft.bonestruct.visual;

import com.google.common.collect.Iterators;
import javafx.geometry.Point2D;

import java.util.Iterator;

public abstract class VisualNode {
	VisualNodeParent parent;

	public abstract Point2D end();

	public abstract javafx.scene.Node visual();

	Iterator<VisualNode> children() {
		return Iterators.forArray();
	}

	void layoutInitial() {
	}

	public abstract void offset(Point2D offset);

	public abstract int treeCompactPriority();
}
