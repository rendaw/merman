package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.helper.*;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

public class TestLayoutRootArray {
	final public static FreeAtomType one;
	final public static FreeAtomType text;
	final public static Syntax syntax;

	static {
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.build();
		text = new TypeBuilder("text")
				.back(Helper.buildBackDataPrimitive("value"))
				.middlePrimitive("value")
				.frontDataPrimitive("value")
				.build();
		syntax = new SyntaxBuilder("any")
				.type(one)
				.type(text)
				.group("any", new GroupBuilder().type(one).type(text).build())
				.style(new StyleBuilder().tag(new StateTag("compact")).tag(new FreeTag("split")).split(true).build())
				.addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
				.build();
	}

	@Test
	public void testLayoutInitial() {
		new GeneralTestWizard(syntax, new TreeBuilder(one).build()).checkSpaceBrick(0, 0).checkTextBrick(0, 1, "one");
	}

	@Test
	public void testCompact() {
		new GeneralTestWizard(syntax, new TreeBuilder(one).build(), new TreeBuilder(one).build())
				.checkSpaceBrick(0, 0)
				.checkTextBrick(0, 1, "one")
				.checkSpaceBrick(0, 2)
				.checkTextBrick(0, 3, "one")
				.resize(40)
				.checkSpaceBrick(0, 0)
				.checkTextBrick(0, 1, "one")
				.checkSpaceBrick(1, 0)
				.checkTextBrick(1, 1, "one");
	}

	@Test
	public void testDynamicCompact() {
		final Atom text = new TreeBuilder(this.text).add("value", "").build();
		new GeneralTestWizard(syntax, new TreeBuilder(one).build(), text)
				.run(context -> text.data.get("value").selectDown(context))
				.resize(40)
				.checkSpaceBrick(0, 0)
				.checkTextBrick(0, 1, "one")
				.checkSpaceBrick(0, 2)
				.checkTextBrick(0, 3, "")
				.run(context -> {
					context.selection.receiveText(context, "x");
					context.selection.receiveText(context, "x");
					context.selection.receiveText(context, "x");
				})
				.checkSpaceBrick(0, 0)
				.checkTextBrick(0, 1, "one")
				.checkSpaceBrick(1, 0)
				.checkTextBrick(1, 1, "xxx");
	}
}
