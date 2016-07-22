package com.zarbosoft.bonestruct.model.middle;

import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.luxemj.Luxem;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "array")
public class DataArray extends DataElement {
	@Luxem.Configuration
	public String tag;

	public List<Node> get(final Map<String, Object> data) {
		return (List<Node>) data.get(key);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		if (!arrayNodes.contains(tag))
			throw new InvalidSyntax(String.format("Unknown node or tag id [%s].", tag));
	}
}
