package com.zarbosoft.bonestruct.ui;

import java.util.List;

import javafx.geometry.Point2D;

public interface UINode {
	Point2D end();
	void split(double edge);
	boolean unsplit(double edge);
	boolean isSplit();
	Point2D size();
	UINode parent();
	List<UINode> children();
}
