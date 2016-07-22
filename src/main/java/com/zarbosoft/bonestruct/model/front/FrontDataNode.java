package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataNode;
import com.zarbosoft.bonestruct.visual.VisualNode;
import com.zarbosoft.luxemj.Luxem;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "node")
public class FrontDataNode implements FrontPart {

	@Luxem.Configuration
	public String key;
	private DataNode dataType;

	@Override
	public VisualNode createVisual(final Map<String, Object> data) {
		return dataType.get(data).createVisual();
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		dataType = nodeType.getDataNode(key);
	}
}
