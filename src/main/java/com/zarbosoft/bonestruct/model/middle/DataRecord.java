package com.zarbosoft.bonestruct.model.middle;

import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "record")
public class DataRecord extends DataElement {
	@Luxem.Configuration
	public String tag;

	public ObservableList<Pair<StringProperty, Node>> get(final Map<String, Object> data) {
		return (ObservableList<Pair<StringProperty, Node>>) data.get(id);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		if (!singleNodes.contains(tag))
			throw new InvalidSyntax(String.format("Unknown unit node or tag id [%s].", tag));
	}
}
