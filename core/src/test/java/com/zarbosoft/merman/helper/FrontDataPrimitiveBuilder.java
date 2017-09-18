package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.front.FrontDataPrimitive;

import java.util.HashSet;
import java.util.Set;

public class FrontDataPrimitiveBuilder {
	private final FrontDataPrimitive front;
	private final Set<String> tags = new HashSet<>();

	public FrontDataPrimitiveBuilder(final String middle) {
		this.front = new FrontDataPrimitive();
		front.middle = middle;
		front.tags(tags);
	}

	public FrontDataPrimitive build() {
		return front;
	}

	public FrontDataPrimitiveBuilder tag(final String tag) {
		tags.add(tag);
		return this;
	}
}
