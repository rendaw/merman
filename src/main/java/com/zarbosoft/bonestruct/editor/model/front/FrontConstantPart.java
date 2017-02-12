package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public abstract class FrontConstantPart extends FrontPart {

	public abstract VisualNodePart createVisual(Context context, Set<VisualNode.Tag> tags);

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return createVisual(context, tags);
	}
}
