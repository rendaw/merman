package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.helper.GeneralTestWizard;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.StyleBuilder;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

public class TestLayoutRootArray {
	final public static FreeAtomType one;
	final public static FreeAtomType text;
	final public static Syntax syntax;

	static {
		one = new Helper.TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new Helper.FrontMarkBuilder("one").build())
				.build();
		text = new Helper.TypeBuilder("text")
				.back(Helper.buildBackDataPrimitive("value"))
				.middlePrimitive("value")
				.frontDataPrimitive("value")
				.build();
		syntax = new Helper.SyntaxBuilder("any")
				.type(one)
				.type(text)
				.group("any", new Helper.GroupBuilder().type(one).type(text).build())
				.style(new StyleBuilder().tag(new StateTag("compact")).tag(new FreeTag("split")).broken(true).build())
				.build();
		syntax.rootFront.prefix.add(new Helper.FrontSpaceBuilder().tag("split").build());
	}

	@Test
	public void testLayoutInitial() {
		new GeneralTestWizard(syntax, new Helper.TreeBuilder(one).build())
				.checkSpaceBrick(0, 0)
				.checkTextBrick(0, 1, "one");
	}

	@Test
	public void testCompact() {
		new GeneralTestWizard(syntax, new Helper.TreeBuilder(one).build(), new Helper.TreeBuilder(one).build())
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
		final Atom text = new Helper.TreeBuilder(this.text).add("value", "").build();
		new GeneralTestWizard(syntax, new Helper.TreeBuilder(one).build(), text)
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
