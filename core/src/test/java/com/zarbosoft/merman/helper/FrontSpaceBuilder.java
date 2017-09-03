package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.symbol.SymbolSpace;

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
