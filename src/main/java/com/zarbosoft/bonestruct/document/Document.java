package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.syntax.Syntax;

public class Document {

	final public Syntax syntax;
	final public ValueArray top;

	public Document(final Syntax syntax, final ValueArray top) {
		this.syntax = syntax;
		this.top = top;
	}
}
