package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.luxemj.Luxem;

import java.util.Set;

@Luxem.Configuration
public abstract class DataElement {
	public String id;

	public abstract void finish(Set<String> singleNodes, Set<String> arrayNodes);

	public abstract Value create();

	public static abstract class Parent {
		public abstract Node node();
	}

	public static abstract class Value {
		public abstract void setParent(Parent parent);

		public abstract Parent parent();

		public abstract Path getPath();
	}

	// TODO
	/*
	@Luxem.Configuration(optional = true)
	boolean optional = false;
	 */
}
