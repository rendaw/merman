package com.zarbosoft.bonestruct.model.middle;

import com.zarbosoft.bonestruct.Luxem;
import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "primitive")
public class DataPrimitive extends DataElement {
	public StringProperty get(final Map<String, Object> data) {
		return (StringProperty) data.get(id);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {

	}
}
