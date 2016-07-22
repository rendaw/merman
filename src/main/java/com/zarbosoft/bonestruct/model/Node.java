package com.zarbosoft.bonestruct.model;

import com.zarbosoft.bonestruct.visual.VisualNode;

import java.util.Map;

public class Node {
	public NodeType type;
	public Map<String, Object> data;

	public VisualNode createVisual() {
		return type.createVisual(data);
	}
}
