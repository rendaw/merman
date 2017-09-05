package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.helper.*;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

public class TestCompaction {
	final public static FreeAtomType one;
	final public static FreeAtomType initialSplitOne;
	final public static FreeAtomType text;
	final public static FreeAtomType comboText;
	final public static FreeAtomType initialSplitText;
	final public static FreeAtomType infinity;
	final public static FreeAtomType line;
	final public static FreeAtomType low;
	final public static FreeAtomType unary;
	final public static FreeAtomType mid;
	final public static FreeAtomType high;
	final public static Syntax syntax;

	static {
		infinity = new TypeBuilder("infinity")
				.back(Helper.buildBackPrimitive("infinity"))
				.front(new FrontMarkBuilder("infinity").build())
				.build();
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.build();
		initialSplitOne = new TypeBuilder("splitOne")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").tag("split").build())
				.build();
		text = new TypeBuilder("text")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		comboText = new TypeBuilder("comboText")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.frontMark("123")
				.build();
		initialSplitText = new TypeBuilder("splitText")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.front(new FrontDataPrimitiveBuilder("value").tag("split").build())
				.build();
		line = new TypeBuilder("line")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value").addPrefix(new FrontSpaceBuilder().build()).build())
				.precedence(0)
				.depthScore(1)
				.build();
		low = new TypeBuilder("low")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value")
						.addPrefix(new FrontSpaceBuilder().tag("split").build())
						.build())
				.precedence(0)
				.depthScore(1)
				.build();
		unary = new TypeBuilder("unary")
				.middleNode("value", "any")
				.back(Helper.buildBackDataNode("value"))
				.frontDataNode("value")
				.precedence(20)
				.depthScore(1)
				.build();
		mid = new TypeBuilder("mid")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value")
						.addPrefix(new FrontSpaceBuilder().tag("split").build())
						.build())
				.precedence(50)
				.depthScore(1)
				.build();
		high = new TypeBuilder("high")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value")
						.addPrefix(new FrontSpaceBuilder().tag("split").build())
						.build())
				.precedence(100)
				.depthScore(1)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(one)
				.type(initialSplitOne)
				.type(text)
				.type(comboText)
				.type(initialSplitText)
				.type(infinity)
				.type(line)
				.type(low)
				.type(unary)
				.type(mid)
				.type(high)
				.group("any",
						new GroupBuilder()
								.type(infinity)
								.type(one)
								.type(initialSplitOne)
								.type(line)
								.type(low)
								.type(unary)
								.type(mid)
								.type(high)
								.type(text)
								.type(comboText)
								.type(initialSplitText)
								.build()
				)
				.style(new StyleBuilder().tag(new FreeTag("split")).tag(new StateTag("compact")).split(true).build())
				.build();
		syntax.ellipsizeThreshold = 2;
	}

	@Test
	public void testSplitOnResize() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(low)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build()
		)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "one")
				.resize(40)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "one")
				.resize(10_000_000)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "one");
	}

	@Test
	public void testSplitOrder() {
		new GeneralTestWizard(syntax, new TreeBuilder(low).addArray("value",
				new TreeBuilder(high)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build(),
				new TreeBuilder(one).build()
		).build())
				.checkTextBrick(0, 2, "one")
				.checkTextBrick(0, 4, "one")
				.checkTextBrick(0, 6, "one")
				.resize(70)
				.checkTextBrick(0, 2, "one")
				.checkTextBrick(0, 4, "one")
				.checkTextBrick(1, 1, "one")
				.resize(40)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 1, "one")
				.resize(70)
				.checkTextBrick(0, 2, "one")
				.checkTextBrick(0, 4, "one")
				.checkTextBrick(1, 1, "one")
				.resize(10_000_000)
				.checkTextBrick(0, 2, "one")
				.checkTextBrick(0, 4, "one")
				.checkTextBrick(0, 6, "one");
	}

	@Test
	public void testSplitOrderInverted() {
		new GeneralTestWizard(syntax, new TreeBuilder(high).addArray("value",
				new TreeBuilder(low)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build(),
				new TreeBuilder(one).build()
		).build())
				.resize(70)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(2, 3, "one")
				.resize(40)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 1, "one")
				.resize(70)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(2, 3, "one")
				.resize(10_000_000)
				.checkTextBrick(0, 2, "one")
				.checkTextBrick(0, 4, "one")
				.checkTextBrick(0, 6, "one");
	}

	@Test
	public void testSplitOrderRule() {
		new GeneralTestWizard(syntax, new TreeBuilder(mid).addArray("value",
				new TreeBuilder(low)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build(),
				new TreeBuilder(high).addArray("value",
						new TreeBuilder(one).build(),
						new TreeBuilder(one).build(),
						new TreeBuilder(one).build()
				).build()
		).build())
				.resize(110)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 2, "one")
				.checkTextBrick(3, 4, "one")
				.checkTextBrick(3, 6, "one")
				.resize(80)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(4, 1, "one")
				.checkTextBrick(5, 1, "one")
				.checkTextBrick(6, 1, "one")
				.resize(40)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(4, 1, "one")
				.checkTextBrick(5, 1, "one")
				.checkTextBrick(6, 1, "one")
				.resize(80)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(4, 1, "one")
				.checkTextBrick(5, 1, "one")
				.checkTextBrick(6, 1, "one")
				.resize(110)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 2, "one")
				.checkTextBrick(3, 4, "one")
				.checkTextBrick(3, 6, "one")
				.resize(10_000_000)
				.checkTextBrick(0, 2, "one")
				.checkTextBrick(0, 4, "one")
				.checkTextBrick(0, 7, "one")
				.checkTextBrick(0, 9, "one")
				.checkTextBrick(0, 11, "one");
	}

	@Test
	public void testSplitDynamic() {
		final Atom lowAtom = new TreeBuilder(low)
				.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
				.build();
		final ValueArray array = (ValueArray) lowAtom.data.get("value");
		new GeneralTestWizard(syntax, lowAtom)
				.resize(70)
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "one")
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 2, 0, ImmutableList.of(new TreeBuilder(one).build()))
				))
				.checkCourseCount(3)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.run(context -> context.history.apply(context, new ChangeArray(array, 2, 1, ImmutableList.of())))
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "one");
	}

	@Test
	public void testSplitDynamicBrickChange() {
		final Atom textAtom = new TreeBuilder(text).add("value", "oran").build();
		final ValuePrimitive text = (ValuePrimitive) textAtom.data.get("value");
		new GeneralTestWizard(syntax,
				new TreeBuilder(low).addArray("value", new TreeBuilder(one).build(), textAtom).build()
		)
				.resize(80)
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "oran")
				.run(context -> context.history.apply(context, new ChangePrimitiveAdd(text, 4, "ge")))
				.checkCourseCount(2)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "orange")
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(text, 4, 2)))
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "oran");
	}

	@Test
	public void testSplitDynamicComboBrick() {
		final Atom primitive = new TreeBuilder(comboText).add("value", "I am a banana").build();
		new GeneralTestWizard(syntax, primitive)
				.resize(140)
				.checkTextBrick(0, 0, "I am a banana")
				.checkTextBrick(0, 1, "123")
				.run(context -> context.history.apply(context,
						new ChangePrimitiveAdd((ValuePrimitive) primitive.data.get("value"), 0, "wigwam ")
				))
				.checkTextBrick(0, 0, "wigwam I am a ")
				.checkTextBrick(1, 0, "banana")
				.checkTextBrick(1, 1, "123");
	}

	@Test
	public void testExpandPrimitiveOrder() {
		new GeneralTestWizard(syntax, new TreeBuilder(low).addArray("value",
				new TreeBuilder(one).build(),
				new TreeBuilder(mid)
						.addArray("value", new TreeBuilder(initialSplitText).add("value", "zet xor").build())
						.build(),
				new TreeBuilder(one).build()
		).build())
				.resize(130)
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 4, "zet xor")
				.checkTextBrick(0, 6, "one")
				.resize(40)
				.checkCourseCount(6)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(3, 0, "zet ")
				.checkTextBrick(4, 0, "xor")
				.checkTextBrick(5, 1, "one")
				.resize(130)
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 4, "zet xor")
				.checkTextBrick(0, 6, "one");
	}

	@Test
	public void testExpandEdge() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(line)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(initialSplitOne).build())
						.build()
		).resize(10).checkCourseCount(2).resize(50).checkCourseCount(2);
	}

	@Test
	public void testSplitNested() {
		final Atom lowAtom = new TreeBuilder(low)
				.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
				.build();
		final ValueArray array = (ValueArray) lowAtom.data.get("value");
		new GeneralTestWizard(syntax, new TreeBuilder(unary).add("value", lowAtom).build())
				.resize(70)
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "one")
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 2, 0, ImmutableList.of(new TreeBuilder(one).build()))
				))
				.checkCourseCount(3)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.run(context -> context.history.apply(context, new ChangeArray(array, 2, 1, ImmutableList.of())))
				.checkCourseCount(1)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 3, "one");
	}

	@Test
	public void testSplitOrderRuleDynamic() {
		final Atom highAtom = new TreeBuilder(high)
				.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
				.build();
		final ValueArray array = (ValueArray) highAtom.data.get("value");
		new GeneralTestWizard(syntax, new TreeBuilder(mid).addArray("value",
				new TreeBuilder(low)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build(),
				highAtom
		).build())
				.resize(80)
				.checkCourseCount(4)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 2, "one")
				.checkTextBrick(3, 4, "one")
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 2, 0, ImmutableList.of(new TreeBuilder(one).build()))
				))
				.checkCourseCount(7)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(4, 1, "one")
				.checkTextBrick(5, 1, "one")
				.checkTextBrick(6, 1, "one")
				.run(context -> context.history.apply(context, new ChangeArray(array, 2, 1, ImmutableList.of())))
				.checkCourseCount(4)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 2, "one")
				.checkTextBrick(3, 4, "one");
	}

	@Test
	public void testStartCompactDynamic() {
		final Atom lowAtom = new TreeBuilder(low)
				.addArray("value",
						new TreeBuilder(one).build(),
						new TreeBuilder(one).build(),
						new TreeBuilder(infinity).build()
				)
				.build();
		final ValueArray array = (ValueArray) lowAtom.data.get("value");
		new GeneralTestWizard(syntax, lowAtom)
				.resize(100)
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "infinity")
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 1, 0, ImmutableList.of(new TreeBuilder(one).build()))
				))
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 1, "infinity");
	}

	@Test
	public void testCompactWindowDownSimple() {
		final Atom midAtom = new TreeBuilder(mid).addArray("value",
				new TreeBuilder(one).build(),
				new TreeBuilder(high)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build(),
				new TreeBuilder(one).build()
		).build();
		new GeneralTestWizard(syntax, new TreeBuilder(low).addArray("value", midAtom).build())
				.act("window")
				.resize(70)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "...")
				.checkTextBrick(3, 1, "one")
				.run(context -> midAtom.parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 2, "one")
				.checkTextBrick(1, 4, "one")
				.checkTextBrick(2, 1, "one");
	}

	@Test
	public void testCompactWindowDown() {
		final Atom midAtom = new TreeBuilder(mid).addArray("value",
				new TreeBuilder(one).build(),
				new TreeBuilder(low)
						.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
						.build(),
				new TreeBuilder(one).build()
		).build();
		new GeneralTestWizard(syntax, new TreeBuilder(high).addArray("value", midAtom).build())
				.act("window")
				.resize(70)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "...")
				.checkTextBrick(3, 1, "one")
				.run(context -> midAtom.parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(2, 3, "one");
	}

	@Test
	public void testIdentical() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(low)
						.addArray("value", new TreeBuilder(infinity).build(), new TreeBuilder(high).addArray("value",
								new TreeBuilder(low)
										.addArray("value", new TreeBuilder(one).build(), new TreeBuilder(one).build())
										.build()
						).build(), new TreeBuilder(infinity).build())
						.build()
		)
				.checkTextBrick(0, 1, "infinity")
				.checkTextBrick(0, 5, "one")
				.checkTextBrick(0, 7, "one")
				.checkTextBrick(0, 9, "infinity")
				.resize(100)
				.checkTextBrick(0, 1, "infinity")
				.checkTextBrick(1, 3, "one")
				.checkTextBrick(1, 5, "one")
				.checkTextBrick(2, 1, "infinity")
				.resize(50)
				.checkTextBrick(0, 1, "infinity")
				.checkTextBrick(2, 1, "one")
				.checkTextBrick(3, 1, "one")
				.checkTextBrick(4, 1, "infinity")
				.resize(100)
				.checkTextBrick(0, 1, "infinity")
				.checkTextBrick(1, 3, "one")
				.checkTextBrick(1, 5, "one")
				.checkTextBrick(2, 1, "infinity")
				.resize(10_000_000)
				.checkTextBrick(0, 1, "infinity")
				.checkTextBrick(0, 5, "one")
				.checkTextBrick(0, 7, "one")
				.checkTextBrick(0, 9, "infinity");
	}
}
