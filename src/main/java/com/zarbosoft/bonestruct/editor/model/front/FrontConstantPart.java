package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.Luxem;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.VisualNodePart;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public abstract class FrontConstantPart extends FrontPart {

	public abstract VisualNodePart createVisual(Context context, Set<VisualNode.Tag> tags);

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, Object> data, final Set<VisualNode.Tag> tags
	) {
		return createVisual(context, tags);
	}
}
