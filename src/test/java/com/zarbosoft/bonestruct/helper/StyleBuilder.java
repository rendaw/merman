package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.style.Style;

public class StyleBuilder {
	Style style = new Style();

	public StyleBuilder tag(final Visual.Tag tag) {
		style.with.add(tag);
		return this;
	}

	public StyleBuilder broken(final boolean on) {
		style.broken = on;
		return this;
	}

	public Style build() {
		return style;
	}
}
