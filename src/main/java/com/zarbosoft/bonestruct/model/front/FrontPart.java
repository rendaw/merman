package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public abstract class FrontPart {

	@Luxem.Configuration
	public Set<String> tags;

	public abstract VisualNodePart createVisual(Context context, Map<String, Object> data, Set<VisualNode.Tag> tags);

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
	}
}
