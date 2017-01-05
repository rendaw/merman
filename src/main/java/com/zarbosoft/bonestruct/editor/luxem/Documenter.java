package com.zarbosoft.bonestruct.editor.luxem;

import j2html.tags.Tag;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static j2html.TagCreator.*;

public class Documenter extends com.zarbosoft.luxemj.Documenter {
	static private Documenter instance = null;

	public static Documenter get() {
		if (instance == null)
			instance = new Documenter();
		return instance;
	}

	@Override
	public Tag documentValuesOf(
			final Luxem.TypeInfo target, final Map<Type, Tag> document, final List<String> shorten
	) {
		if (target.inner == Color.class) {
			return span().with(code().withText("[R, G, B]"), text(" where R, G, B are double values between 0 and 1."));
		}
		return super.documentValuesOf(target, document, shorten);
	}
}
