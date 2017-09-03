package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.back.BackArray;
import com.zarbosoft.merman.syntax.back.BackPart;

public class BackArrayBuilder {
	BackArray back = new BackArray();

	public BackArrayBuilder add(final BackPart part) {
		back.elements.add(part);
		return this;
	}

	public BackPart build() {
		return back;
	}
}
