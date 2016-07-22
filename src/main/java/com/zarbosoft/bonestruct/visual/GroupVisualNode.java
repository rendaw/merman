package com.zarbosoft.bonestruct.visual;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class GroupVisualNode extends VisualNode {
	int treeCompactPriority = 0;
	List<VisualNode> children = new ArrayList<>();
	Point2D end = new Point2D(0, 0);
	Pane pane = new Pane();

	public void add(final VisualNode node) {
		node.parent = new VisualNodeParent() {

			@Override
			public VisualNodeParent parent() {
				if (parent == null)
					return null;
				return parent;
			}

			@Override
			public void updatePriority(final int treeCompactPriority) {

			}
		};
		if (children.isEmpty())
			treeCompactPriority = node.treeCompactPriority();
		else
			treeCompactPriority = Math.max(treeCompactPriority, node.treeCompactPriority());
		for (VisualNodeParent at = parent; at != null; at = at.parent()) {
			at.updatePriority(treeCompactPriority);
		}
		this.children.add(node);
		pane.getChildren().add(node.visual());
	}

	@Override
	public Point2D end() {
		return end;
	}

	@Override
	public javafx.scene.Node visual() {
		return pane;
	}

	@Override
	public Iterator<VisualNode> children() {
		return children.iterator();
	}

	@Override
	public void layoutInitial() {
		end = Point2D.ZERO;
		for (final VisualNode child : children) {
			child.offset(end);
			end = end.add(child.end());
		}
	}

	@Override
	public void offset(final Point2D offset) {
		pane.setTranslateX(offset.getX());
		pane.setTranslateY(offset.getY());
	}

	@Override
	public int treeCompactPriority() {
		return treeCompactPriority;
	}
}
