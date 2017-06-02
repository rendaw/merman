package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.symbol.SymbolSpace;

public class FrontSpaceBuilder {
	private final FrontSymbol front;

	public FrontSpaceBuilder() {
		this.front = new FrontSymbol();
		front.type = new SymbolSpace();
	}

	public FrontSymbol build() {
		return front;
	}

	public FrontSpaceBuilder tag(final String tag) {
		front.tags.add(tag);
		return this;
	}
}
