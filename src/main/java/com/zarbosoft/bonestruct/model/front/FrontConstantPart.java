package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;

import java.util.Map;

@Luxem.Configuration
public interface FrontConstantPart extends FrontPart {

	VisualNodePart createVisual(Context context);

	@Override
	default VisualNodePart createVisual(final Context context, final Map<String, Object> data) {
		return createVisual(context);
	}
}
