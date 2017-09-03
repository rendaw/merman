package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontDataArray;
import com.zarbosoft.merman.syntax.front.FrontSymbol;

public class FrontDataArrayBuilder {
	private final FrontDataArray front;

	public FrontDataArrayBuilder(final String middle) {
		this.front = new FrontDataArray();
		front.middle = middle;
	}

	public FrontDataArray build() {
		return front;
	}

	public FrontDataArrayBuilder addSeparator(final FrontSymbol part) {
		front.separator.add(part);
		return this;
	}

	public FrontDataArrayBuilder addPrefix(final FrontSymbol part) {
		front.prefix.add(part);
		return this;
	}
}
