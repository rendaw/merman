package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.visual.VisualNode;

import java.util.Map;

@Luxem.Configuration
public interface FrontConstantPart extends FrontPart {

	VisualNode createVisual();

	@Override
	default VisualNode createVisual(final Map<String, Object> data) {
		return createVisual();
	}
}
