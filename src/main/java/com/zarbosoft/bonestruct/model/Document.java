package com.zarbosoft.bonestruct.model;

public class Document {
	
	private Syntax syntax;
	public Node root;

	public Document(Syntax syntax, Node root) {
		this.syntax = syntax;
		this.root = root;
	}

}
