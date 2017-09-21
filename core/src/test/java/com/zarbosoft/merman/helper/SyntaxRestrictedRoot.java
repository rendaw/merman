package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;

public class SyntaxRestrictedRoot {
	final public static FreeAtomType one;
	final public static FreeAtomType quoted;
	final public static FreeAtomType binaryBang;
	final public static Syntax syntax;

	static {
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.autoComplete(-1)
				.build();
		quoted = new TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.front(new FrontMarkBuilder("\"").build())
				.frontDataPrimitive("value")
				.front(new FrontMarkBuilder("\"").build())
				.autoComplete(1)
				.build();
		binaryBang = new TypeBuilder("bang")
				.middleAtom("first", "one")
				.middleAtom("second", "one")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataAtom("first"))
						.add("second", Helper.buildBackDataAtom("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.autoComplete(1)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(quoted)
				.type(one)
				.type(binaryBang)
				.group("restricted_group", new GroupBuilder().type(quoted).build())
				.group("any", new GroupBuilder().type(quoted).type(binaryBang).build())
				.build();
	}
}
