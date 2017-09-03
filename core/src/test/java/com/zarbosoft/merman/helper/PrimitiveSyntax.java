package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;

public class PrimitiveSyntax {
	public final static FreeAtomType primitive;
	public final static FreeAtomType low;
	public final static FreeAtomType high;
	public final static FreeAtomType quoted;
	public final static FreeAtomType array;
	public final static Syntax syntax;

	static {
		primitive = new TypeBuilder("primitive")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.autoComplete(99)
				.build();
		low = new TypeBuilder("low")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.precedence(0)
				.build();
		high = new TypeBuilder("high")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.precedence(100)
				.build();
		quoted = new TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontMark("\"")
				.frontDataPrimitive("value")
				.frontMark("\"")
				.autoComplete(99)
				.build();
		array = new TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value").build())
				.autoComplete(99)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(primitive)
				.type(low)
				.type(high)
				.type(quoted)
				.type(array)
				.group("any", new GroupBuilder().type(primitive).type(low).type(high).type(quoted).type(array).build())
				.style(new StyleBuilder().tag(new StateTag("compact")).tag(new FreeTag("split")).split(true).build())
				.addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
				.build();
		syntax.retryExpandFactor = 1.05;
	}
}
