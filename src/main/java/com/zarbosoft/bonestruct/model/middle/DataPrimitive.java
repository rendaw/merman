package com.zarbosoft.bonestruct.model.middle;

import com.zarbosoft.bonestruct.Luxem;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "primitive")
public class DataPrimitive extends DataElement {
	public String get(final Map<String, Object> data) {
		return (String) data.get(key);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {

	}
}
