package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;

import java.util.Map;

public class Node {
	public NodeType type;
	public Map<String, Object> data;

	public VisualNode createVisual(final Context context) {
		return type.createVisual(context, data);
	}
}
