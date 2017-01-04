package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.luxemj.Luxem;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "array")
public class DataArray extends DataElement {
	@Luxem.Configuration
	public String tag;

	public ObservableList<Node> get(final Map<String, Object> data) {
		return (ObservableList<Node>) data.get(id);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		if (!arrayNodes.contains(tag))
			throw new InvalidSyntax(String.format("Unknown node or tag id [%s].", tag));
	}
}
