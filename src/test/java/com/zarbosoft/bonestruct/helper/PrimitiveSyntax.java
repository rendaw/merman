package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;

public class PrimitiveSyntax {
	public final static FreeAtomType primitive;
	public final static FreeAtomType quoted;
	public final static Syntax syntax;

	static {
		primitive = new TypeBuilder("primitive")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.autoComplete(99)
				.build();
		quoted = new TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontMark("\"")
				.frontDataPrimitive("value")
				.frontMark("\"")
				.autoComplete(99)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(primitive)
				.type(quoted)
				.group("any", new GroupBuilder().type(primitive).type(quoted).build())
				.style(new StyleBuilder().tag(new StateTag("compact")).tag(new FreeTag("split")).split(true).build())
				.build();
		syntax.rootFront.prefix.add(new FrontSpaceBuilder().tag("split").build());
	}
}
