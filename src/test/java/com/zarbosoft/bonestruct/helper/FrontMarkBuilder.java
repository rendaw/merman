package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.symbol.SymbolText;

public class FrontMarkBuilder {
	private final FrontSymbol front;

	public FrontMarkBuilder(final String value) {
		this.front = new FrontSymbol();
		front.type = new SymbolText(value);
	}

	public FrontSymbol build() {
		return front;
	}

	public FrontMarkBuilder tag(final String tag) {
		front.tags.add(tag);
		return this;
	}
}
