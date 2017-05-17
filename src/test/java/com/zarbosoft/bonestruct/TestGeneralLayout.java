package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.helper.GeneralTestWizard;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.StyleBuilder;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import static com.zarbosoft.bonestruct.helper.Helper.dump;

public class TestGeneralLayout {
	final public static FreeAtomType one;
	final public static FreeAtomType two;
	final public static FreeAtomType array;
	final public static Syntax syntax;

	static {
		one = new Helper.TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new Helper.FrontMarkBuilder("one").build())
				.build();
		two = new Helper.TypeBuilder("two")
				.back(Helper.buildBackPrimitive("two"))
				.front(new Helper.FrontMarkBuilder("two").build())
				.build();
		array = new Helper.TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontMark("[")
				.front(new Helper.FrontDataArrayBuilder("value")
						.addSeparator(new Helper.FrontMarkBuilder(", ").build())
						.build())
				.frontMark("]")
				.autoComplete(99)
				.build();
		syntax = new Helper.SyntaxBuilder("any")
				.type(one)
				.type(two)
				.type(array)
				.group("any", new Helper.GroupBuilder().type(one).type(two).type(array).build())
				.style(new StyleBuilder().broken(true).build())
				.style(new StyleBuilder().tag(new Visual.PartTag("separator")).broken(false).build())
				.build();
	}

	@Test
	public void testInitialLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(two).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build()
		).checkScroll(-10).checkCourse(0, 0, 10).checkCourse(1, 17, 27).checkBanner(8, 10).checkDetails(20, 27);
	}

	@Test
	public void testClippedLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(two).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build()
		).resizeTransitive(40).checkScroll(-10).checkBanner(8, 10).checkDetails(20, 27);
	}

	@Test
	public void testScrollLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(two).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build()
		)
				.resizeTransitive(40)
				.run(context -> {
					context.document.top.data.get(4).parent.selectUp(context);
				})
				.checkScroll(24)
				.checkCourse(4, 47, 57)
				.checkCourse(3, 27, 37)
				.checkCourse(5, 64, 74)
				.checkBanner(21, 23)
				.checkDetails(33, 40);
	}

	@Test
	public void testStaticArrayLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(array)
						.addArray("value", new Helper.TreeBuilder(one).build(), new Helper.TreeBuilder(two).build())
						.build()
		)
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(1, 0, "one")
				.checkTextBrick(1, 1, ", ")
				.checkTextBrick(2, 0, "two")
				.checkTextBrick(3, 0, "]");
	}

	@Test
	public void testDynamicArrayLayout() {
		final Atom gap = syntax.gap.create();
		new GeneralTestWizard(syntax, gap).run(context -> {
			gap.data.get("gap").selectDown(context);
			context.selection.receiveText(context, "[");
			dump(context.document.top);
		}).checkTextBrick(0, 0, "[").checkTextBrick(1, 0, "").checkTextBrick(2, 0, "]");
	}
}
