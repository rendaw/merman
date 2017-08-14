package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.helper.*;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

public class TestLayoutGeneral {
	final public static FreeAtomType one;
	final public static FreeAtomType two;
	final public static FreeAtomType text;
	final public static FreeAtomType array;
	final public static Syntax syntax;

	static {
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.build();
		two = new TypeBuilder("two")
				.back(Helper.buildBackPrimitive("two"))
				.front(new FrontMarkBuilder("two").build())
				.build();
		text = new TypeBuilder("text")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		array = new TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontMark("[")
				.front(new FrontDataArrayBuilder("value")
						.addSeparator(new FrontMarkBuilder(", ").tag("separator").build())
						.build())
				.frontMark("]")
				.autoComplete(99)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(one)
				.type(two)
				.type(text)
				.type(array)
				.group("any", new GroupBuilder().type(one).type(two).type(text).type(array).build())
				.style(new StyleBuilder().split(true).build())
				.style(new StyleBuilder().tag(new FreeTag("separator")).split(false).build())
				.build();
	}

	@Test
	public void testInitialLayout() {
		new GeneralTestWizard(
				syntax,
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(two).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build()
		).checkScroll(-10).checkCourse(0, 0, 10).checkCourse(1, 17, 27).checkBanner(8, 10).checkDetails(20, 27);
	}

	@Test
	public void testClippedLayout() {
		new GeneralTestWizard(
				syntax,
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(two).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build()
		).resizeTransitive(40).checkScroll(-10).checkBanner(8, 10).checkDetails(20, 27);
	}

	@Test
	public void testScrollLayout() {
		new GeneralTestWizard(
				syntax,
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(two).build(),
				new TreeBuilder(one).build(),
				new TreeBuilder(one).build()
		)
				.resizeTransitive(40)
				.run(context -> {
					Helper.rootArray(context.document).data.get(4).parent.selectUp(context);
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
				new TreeBuilder(array)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(two).build())
						.build()
		)
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(1, 0, "one")
				.checkTextBrick(1, 1, ", ")
				.checkTextBrick(2, 0, "two")
				.checkTextBrick(3, 0, "]");
	}

	@Test
	public void testDynamicWrapLayout() {
		final Atom text = new TreeBuilder(this.text).add("value", "123").build();
		new GeneralTestWizard(syntax, text)
				.run(context -> text.data.get("value").selectDown(context))
				.resize(40)
				.run(context -> context.selection.receiveText(context, "4"))
				.checkCourseCount(1)
				.checkCourse(0, 0, 10)
				.checkBrickNotCompact(0, 0)
				.run(context -> context.selection.receiveText(context, "5"))
				.checkCourseCount(2)
				.checkCourse(0, -20, -10)
				.checkCourse(1, 0, 10)
				.checkBrickCompact(0, 0)
				.run(context -> context.selection.receiveText(context, "6"))
				.checkCourseCount(2)
				.checkCourse(0, -20, -10)
				.checkCourse(1, 0, 10);
	}

	@Test
	public void testDynamicUnwrapLayout() {
		final Atom text = new TreeBuilder(this.text).add("value", "123456").build();
		final ValuePrimitive primitive = (ValuePrimitive) text.data.get("value");
		new GeneralTestWizard(syntax, text)
				.run(context -> text.data.get("value").selectDown(context))
				.resize(40)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 5, 1)))
				.checkCourseCount(2)
				.checkCourse(0, -20, -10)
				.checkCourse(1, 0, 10)
				.checkBrickCompact(0, 0)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 4, 1)))
				.checkCourseCount(1)
				.checkCourse(0, -20, -10)
				.checkBrickNotCompact(0, 0)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 3, 1)))
				.checkCourseCount(1)
				.checkCourse(0, -20, -10)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(primitive, 2, 1)))
				.checkCourseCount(1)
				.checkCourse(0, -20, -10)
				.checkBrickNotCompact(0, 0);
	}

	@Test
	public void testDynamicArrayLayout() {
		final Atom gap = syntax.gap.create();
		new GeneralTestWizard(syntax, gap).run(context -> {
			gap.data.get("gap").selectDown(context);
			context.selection.receiveText(context, "[");
		}).checkTextBrick(0, 0, "[").checkTextBrick(1, 0, "").checkTextBrick(2, 0, "]");
	}
}
