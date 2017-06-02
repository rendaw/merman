package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.syntax.style.Style;

public class StyleBuilder {
	Style style = new Style();

	public StyleBuilder tag(final Tag tag) {
		style.with.add(tag);
		return this;
	}

	public StyleBuilder split(final boolean on) {
		style.split = on;
		return this;
	}

	public Style build() {
		return style;
	}

	public StyleBuilder alignment(final String name) {
		style.alignment = name;
		return this;
	}
}
