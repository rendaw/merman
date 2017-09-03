package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontDataPrimitive;

public class FrontDataPrimitiveBuilder {
	private final FrontDataPrimitive front;

	public FrontDataPrimitiveBuilder(final String middle) {
		this.front = new FrontDataPrimitive();
		front.middle = middle;
	}

	public FrontDataPrimitive build() {
		return front;
	}

	public FrontDataPrimitiveBuilder tag(final String tag) {
		front.tags.add(tag);
		return this;
	}
}
