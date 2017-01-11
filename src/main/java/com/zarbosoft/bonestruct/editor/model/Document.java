package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.model.middle.DataArray;

public class Document {

	public Syntax syntax;
	public DataArray.Value top;

	public Document(final Syntax syntax, final DataArray.Value top) {
		this.syntax = syntax;
		this.top = top;
	}
}
