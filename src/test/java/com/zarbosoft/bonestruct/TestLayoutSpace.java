package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.helper.GeneralTestWizard;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.StyleBuilder;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

public class TestLayoutSpace {
	final public static FreeAtomType one;
	final public static FreeAtomType array;
	final public static Syntax syntax;

	static {
		one = new Helper.TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.frontSpace()
				.front(new Helper.FrontMarkBuilder("one").build())
				.frontSpace()
				.build();
		array = new Helper.TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontSpace()
				.frontMark("[")
				.front(new Helper.FrontDataArrayBuilder("value")
						.addSeparator(new Helper.FrontMarkBuilder(", ").build())
						.build())
				.frontSpace()
				.frontMark("]")
				.autoComplete(99)
				.build();
		syntax = new Helper.SyntaxBuilder("any")
				.type(one)
				.type(array)
				.group("any", new Helper.GroupBuilder().type(one).type(array).build())
				.style(new StyleBuilder().tag(new PartTag("space")).broken(true).build())
				.build();
	}

	@Test
	public void testLayoutInitial() {
		new GeneralTestWizard(syntax, new Helper.TreeBuilder(one).build())
				.checkSpaceBrick(0, 0)
				.checkTextBrick(0, 1, "one")
				.checkSpaceBrick(1, 0);
	}
}
