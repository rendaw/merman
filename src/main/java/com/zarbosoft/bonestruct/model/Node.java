package com.zarbosoft.bonestruct.model;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;

import java.util.Map;

public class Node {
	public NodeType type;
	public Map<String, Object> data;

	public VisualNode createVisual(final Context context) {
		return type.createVisual(context, data);
	}
}
