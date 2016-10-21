package com.zarbosoft.bonestruct.model.middle;

import com.zarbosoft.luxemj.Luxem;

import java.util.Set;

@Luxem.Configuration
public abstract class DataElement {
	public String id;

	public abstract void finish(Set<String> singleNodes, Set<String> arrayNodes);

	// TODO
	/*
	@Luxem.Configuration(optional = true)
	boolean optional = false;
	 */
}
