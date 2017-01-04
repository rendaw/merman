package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.Luxem;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.VisualNodePart;

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
