package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.visual.VisualNode;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public interface FrontPart {

	VisualNode createVisual(Map<String, Object> data);

	default void finish(final NodeType nodeType, final Set<String> middleUsed) {
	}
}
