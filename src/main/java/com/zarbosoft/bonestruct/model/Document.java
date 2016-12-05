package com.zarbosoft.bonestruct.model;

import javafx.collections.ObservableList;

public class Document {

	public Syntax syntax;
	public ObservableList<Node> top;

	public Document(final Syntax syntax, final ObservableList<Node> top) {
		this.syntax = syntax;
		this.top = top;
	}
}
