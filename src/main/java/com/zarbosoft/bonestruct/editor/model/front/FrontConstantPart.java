package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.interface1.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public abstract class FrontConstantPart extends FrontPart {

	public abstract VisualNodePart createVisual(Context context, Set<VisualNode.Tag> tags);

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return createVisual(context, tags);
	}

	@Override
	final public String middle() {
		return null;
	}
}
