package com.zarbosoft.bonestruct.model;

public class Document {

	public Syntax syntax;
	public Node root;

	public Document(final Syntax syntax, final Node root) {
		this.syntax = syntax;
		this.root = root;
	}
}
