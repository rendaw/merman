package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;

import static com.zarbosoft.bonestruct.Helper.*;

public class PrimitiveSyntax {
	final static FreeNodeType primitive;
	final static Syntax syntax;

	static {
		primitive = new TypeBuilder("primitive")
				.middlePrimitive("value")
				.back(buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.autoComplete(99)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(primitive)
				.group("any", new GroupBuilder().type(primitive).build())
				.build();
	}
}
