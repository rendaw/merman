package com.zarbosoft.bonestruct.model.middle;

import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Node;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "node")
public class DataNode extends DataElement {
	@Luxem.Configuration
	public String tag;

	public Node get(final Map<String, Object> data) {
		return (Node) data.get(key);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		if (!singleNodes.contains(tag))
			throw new InvalidSyntax(String.format("Unknown unit node or tag id [%s].", tag));
	}
}
