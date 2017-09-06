package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.helper.*;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.function.Function;

@RunWith(Parameterized.class)
public class TestLayoutAlignment {

	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() {
		return ImmutableList.of(new Object[] {1}, new Object[] {2}, new Object[] {10});
	}

	final public FreeAtomType primitive;
	final public FreeAtomType relative;
	final public FreeAtomType absolute;
	final public FreeAtomType array;
	final public FreeAtomType compactArray;
	final public FreeAtomType line;
	final public FreeAtomType pair;
	final public FreeAtomType atomPair;
	final public FreeAtomType splitPair;
	final public FreeAtomType triple;
	final public FreeAtomType reverseTriple;
	final public FreeAtomType threeLine;
	final public FreeAtomType threeLine2;
	final public Syntax syntax;

	public TestLayoutAlignment(final int layBrickBatchSize) {
		primitive = new TypeBuilder("primitive")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		relative = new TypeBuilder("relative")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		absolute = new TypeBuilder("absolute")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		array = new TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value")
						.addPrefix(new FrontSpaceBuilder().tag("split").build())
						.build())
				.absoluteAlignment("absolute", 7)
				.relativeAlignment("relative", "relative", 3)
				.build();
		compactArray = new TypeBuilder("compactArray")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value")
						.addPrefix(new FrontSpaceBuilder().tag("compact_split").build())
						.build())
				.build();
		line = new TypeBuilder("line")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value").build())
				.build();
		pair = new TypeBuilder("pair")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackDataPrimitive("first"))
						.add(Helper.buildBackDataPrimitive("second"))
						.build())
				.front(new FrontDataPrimitiveBuilder("first").build())
				.front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
				.build();
		atomPair = new TypeBuilder("atomPair")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackDataNode("first"))
						.add(Helper.buildBackDataNode("second"))
						.build())
				.front(new FrontSpaceBuilder().build())
				.frontDataNode("first")
				.front(new FrontSpaceBuilder().tag("concensus1unsplit").tag("compact_split").build())
				.frontDataNode("second")
				.build();
		splitPair = new TypeBuilder("splitPair")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackDataPrimitive("first"))
						.add(Helper.buildBackDataPrimitive("second"))
						.build())
				.front(new FrontDataPrimitiveBuilder("first").build())
				.front(new FrontDataPrimitiveBuilder("second").tag("concensus1unsplit").tag("compact_split").build())
				.build();
		triple = new TypeBuilder("triple")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.middlePrimitive("third")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackDataPrimitive("first"))
						.add(Helper.buildBackDataPrimitive("second"))
						.add(Helper.buildBackDataPrimitive("third"))
						.build())
				.front(new FrontDataPrimitiveBuilder("first").build())
				.front(new FrontDataPrimitiveBuilder("second").tag("concensus1").build())
				.front(new FrontDataPrimitiveBuilder("third").tag("concensus2").build())
				.build();
		reverseTriple = new TypeBuilder("reverseTriple")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.middlePrimitive("third")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackDataPrimitive("first"))
						.add(Helper.buildBackDataPrimitive("second"))
						.add(Helper.buildBackDataPrimitive("third"))
						.build())
				.front(new FrontDataPrimitiveBuilder("first").build())
				.front(new FrontDataPrimitiveBuilder("second").tag("concensus2").build())
				.front(new FrontDataPrimitiveBuilder("third").tag("concensus1").build())
				.build();
		threeLine = new TypeBuilder("threeLine").back(Helper.buildBackPrimitive("threeLine"))
				/* Line 1 */
				.front(new FrontMarkBuilder("width2").tag("concensus1unsplit").build())
				.front(new FrontMarkBuilder("b").tag("concensus2").build())
				/* Line 2 */
				.front(new FrontSpaceBuilder().tag("split").build())
				.front(new FrontMarkBuilder("width3").tag("compact_split").tag("concensus2unsplit").build())
				/* Line 3 */
				.front(new FrontMarkBuilder("width1").tag("split").build())
				.front(new FrontMarkBuilder("a").tag("concensus1").build())
				.build();
		threeLine2 = new TypeBuilder("threeLine2")
				.back(Helper.buildBackPrimitive("threeLine"))
				.front(new FrontMarkBuilder("line1").build())
				.front(new FrontMarkBuilder("line2").tag("split").build())
				.front(new FrontMarkBuilder("line3").tag("compact_split").build())
				.build();
		syntax = new SyntaxBuilder("any")
				.type(primitive)
				.type(absolute)
				.type(relative)
				.type(array)
				.type(compactArray)
				.type(line)
				.type(pair)
				.type(atomPair)
				.type(splitPair)
				.type(triple)
				.type(reverseTriple)
				.type(threeLine)
				.type(threeLine2)
				.group("any",
						new GroupBuilder()
								.type(primitive)
								.type(absolute)
								.type(relative)
								.type(array)
								.type(compactArray)
								.type(line)
								.type(pair)
								.type(atomPair)
								.type(splitPair)
								.type(triple)
								.type(reverseTriple)
								.type(threeLine)
								.type(threeLine2)
								.build()
				)
				.absoluteAlignment("absolute", 7)
				.relativeAlignment("relative", 3)
				.concensusAlignment("concensus1")
				.concensusAlignment("concensus2")
				.addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
				.style(new StyleBuilder().tag(new FreeTag("split")).split(true).build())
				.style(new StyleBuilder()
						.tag(new FreeTag("compact_split"))
						.tag(new StateTag("compact"))
						.split(true)
						.build())
				.style(new StyleBuilder().tag(new TypeTag("absolute")).alignment("absolute").build())
				.style(new StyleBuilder().tag(new FreeTag("concensus1")).alignment("concensus1").build())
				.style(new StyleBuilder()
						.tag(new FreeTag("concensus1unsplit"))
						.notag(new StateTag("compact"))
						.alignment("concensus1")
						.build())
				.style(new StyleBuilder().tag(new FreeTag("concensus2")).alignment("concensus2").build())
				.style(new StyleBuilder()
						.tag(new FreeTag("concensus2unsplit"))
						.notag(new StateTag("compact"))
						.alignment("concensus2")
						.build())
				.style(new StyleBuilder().tag(new TypeTag("relative")).alignment("relative").build())
				.build();
		syntax.layBrickBatchSize = layBrickBatchSize;
	}

	@Test
	public void testRootAbsoluteAlignment() {
		new GeneralTestWizard(syntax, new TreeBuilder(absolute).add("value", "hi").build()).checkBrick(0, 1, 7);
	}

	@Test
	public void testRootRelativeAlignment() {
		new GeneralTestWizard(syntax, new TreeBuilder(relative).add("value", "hi").build()).checkBrick(0, 1, 3);
	}

	@Test
	public void testAbsoluteAlignment() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(array).addArray("value", new TreeBuilder(absolute).add("value", "hi").build()).build()
		).checkBrick(1, 1, 7);
	}

	@Test
	public void testRelativeAlignment() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(array).addArray("value", new TreeBuilder(relative).add("value", "hi").build()).build()
		).checkBrick(1, 1, 6);
	}

	@Test
	public void testConcensusAlignmentSingle() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(pair).add("first", "three").add("second", "lumbar").build()
		).checkBrick(0, 2, 50);
	}

	@Test
	public void testConcensusAlignmentMultiple() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(pair).add("first", "three").add("second", "lumbar").build(),
				new TreeBuilder(pair).add("first", "elephant").add("second", "minx").build(),
				new TreeBuilder(pair).add("first", "tag").add("second", "peanut").build()
		).checkBrick(0, 2, 80).checkBrick(1, 2, 80).checkBrick(2, 2, 80);
	}

	@Test
	public void testDoubleConcensusAlignmentMultiple() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(triple).add("first", "three").add("second", "lumbar").add("third", "a").build(),
				new TreeBuilder(triple).add("first", "elephant").add("second", "minx").add("third", "b").build(),
				new TreeBuilder(triple).add("first", "tag").add("second", "pedantic").add("third", "c").build()
		).checkBrick(0, 3, 160).checkBrick(1, 3, 160).checkBrick(2, 3, 160);
	}

	@Test
	public void testDynamicSecondShiftOut() {
		final Atom line2 = new TreeBuilder(pair).add("first", "c").add("second", "d").build();
		final ValuePrimitive line2_1 = (ValuePrimitive) line2.data.get("first");
		new GeneralTestWizard(syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
				.run(context -> context.history.apply(context, new ChangePrimitiveAdd(line2_1, 1, "cc")))
				.checkBrick(0, 2, 30);
	}

	@Test
	public void testDynamicFirstShiftOut() {
		final Atom line = new TreeBuilder(pair).add("first", "a").add("second", "b").build();
		final ValuePrimitive text = (ValuePrimitive) line.data.get("first");
		new GeneralTestWizard(syntax, line, new TreeBuilder(pair).add("first", "c").add("second", "d").build())
				.run(context -> context.history.apply(context, new ChangePrimitiveAdd(text, 1, "aa")))
				.checkBrick(0, 2, 30);
	}

	@Test
	public void testDynamicShiftIn() {
		final Atom line2 = new TreeBuilder(pair).add("first", "ccccc").add("second", "d").build();
		final ValuePrimitive line2_1 = (ValuePrimitive) line2.data.get("first");
		new GeneralTestWizard(syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(line2_1, 1, 4)))
				.checkBrick(0, 2, 10);
	}

	@Test
	public void testDynamicAddLine() {
		new GeneralTestWizard(syntax, new TreeBuilder(pair).add("first", "a").add("second", "b").build())
				.run(context -> context.history.apply(context, new ChangeArray(Helper.rootArray(context.document),
						1,
						0,
						ImmutableList.of(new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())
				)))
				.checkBrick(0, 2, 30);
	}

	@Test
	public void testDynamicRemoveLine() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
				new TreeBuilder(pair).add("first", "ccc").add("second", "d").build()
		)
				.run(context -> context.history.apply(context,
						new ChangeArray(Helper.rootArray(context.document), 1, 1, ImmutableList.of())
				))
				.checkBrick(0, 2, 10);
	}

	@Test
	public void testConcensusSameLine() {
		new GeneralTestWizard(syntax, new TreeBuilder(line).addArray("value",
				new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
				new TreeBuilder(pair).add("first", "").add("second", "d").build()
		).build()).checkBrick(0, 2, 10).checkBrick(0, 4, 20);
	}

	@Test
	public void testConcensusSameLineDynamicAdd() {
		final Atom line2 = new TreeBuilder(pair).add("first", "").add("second", "d").build();
		final ValuePrimitive line2_1 = (ValuePrimitive) line2.data.get("first");
		new GeneralTestWizard(syntax,
				new TreeBuilder(line)
						.addArray("value", new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
						.build()
		)
				.run(context -> context.history.apply(context, new ChangePrimitiveAdd(line2_1, 0, "cc")))
				.checkBrick(0, 2, 10)
				.checkBrick(0, 4, 40);
	}

	@Test
	public void testConcensusSameLineDynamicAddPairBefore() {
		final Atom line2 = new TreeBuilder(line)
				.addArray("value", new TreeBuilder(pair).add("first", "ccc").add("second", "d").build())
				.build();
		final ValueArray array = (ValueArray) line2.data.get("value");
		new GeneralTestWizard(syntax, line2)
				.run(context -> context.history.apply(
						context,
						new ChangeArray(array,
								0,
								0,
								ImmutableList.of(new TreeBuilder(pair).add("first", "a").add("second", "b").build())
						)
				))
				.checkBrick(0, 2, 10)
				.checkBrick(0, 4, 50);
	}

	@Test
	public void testConcensusSameLineDynamicRemove() {
		final Atom line2 = new TreeBuilder(pair).add("first", "cc").add("second", "d").build();
		final ValuePrimitive line2_1 = (ValuePrimitive) line2.data.get("first");
		new GeneralTestWizard(syntax,
				new TreeBuilder(line)
						.addArray("value", new TreeBuilder(pair).add("first", "a").add("second", "b").build(), line2)
						.build()
		)
				.run(context -> context.history.apply(context, new ChangePrimitiveRemove(line2_1, 0, 2)))
				.checkBrick(0, 2, 10)
				.checkBrick(0, 4, 20);
	}

	@Test
	public void testConcensusExpand() {
		// It's okay if it doesn't expand when there's a concensus involved until much wider
		new GeneralTestWizard(syntax, new TreeBuilder(compactArray).addArray("value",
				new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
				new TreeBuilder(pair).add("first", "cccc").add("second", "d").build()
		).build())
				.resize(60)
				.checkCourseCount(3)
				.checkTextBrick(1, 1, "a")
				.checkTextBrick(2, 1, "cccc")
				.resize(70)
				.checkCourseCount(3)
				.resize(95)
				.checkCourseCount(3)
				.resize(100)
				.checkCourseCount(1);
	}

	@Test
	public void testConcensusBreak() {
		// The concensus value lingers after breaking, so an element that goes offscreen will stay offscreen at first placement
		// Then the concensus resets greatly reducing the line length, triggers expand -> endless loop
		new GeneralTestWizard(syntax, new TreeBuilder(compactArray).addArray("value",
				new TreeBuilder(primitive).add("value", "one").build(),
				new TreeBuilder(pair).add("first", "two").add("second", "three").build()
		).build())
				.checkCourseCount(1)
				.resize(80)
				.checkCourseCount(3)
				.checkTextBrick(1, 1, "one")
				.checkTextBrick(2, 1, "two")
				.resize(10000)
				.checkCourseCount(1);
	}

	@Test
	public void testConcensusBreak2() {
		// Accidentally found an issue where the primitive doesn't get tagged compact
		new GeneralTestWizard(syntax,
				new TreeBuilder(pair).add("first", "lumberpass").add("second", "ink").build(),
				new TreeBuilder(splitPair).add("first", "dog").add("second", "equifortress").build()
		)
				.checkCourseCount(2)
				.resize(200)
				.checkCourseCount(3)
				.checkTextBrick(0, 1, "lumberpass")
				.checkTextBrick(0, 2, "ink")
				.checkTextBrick(1, 1, "dog")
				.checkTextBrick(2, 0, "equifortress")
				.resize(10000)
				.checkCourseCount(2);
	}

	@Test
	public void testConcensusBreak3() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(pair).add("first", "lumberpass").add("second", "ink").build(),
				new TreeBuilder(atomPair)
						.add("first", new TreeBuilder(primitive).add("value", "dog").build())
						.add("second", new TreeBuilder(primitive).add("value", "equifortress").build())
						.build()
		)
				.checkCourseCount(2)
				.resize(200)
				.resize(205)
				.checkCourseCount(3)
				.checkTextBrick(0, 1, "lumberpass")
				.checkTextBrick(0, 2, "ink")
				.checkTextBrick(1, 2, "dog")
				.checkTextBrick(2, 1, "equifortress");
	}

	@Test
	public void testMultiCourseConcensusLoop() {
		new GeneralTestWizard(syntax,
				new TreeBuilder(triple).add("first", "hower").add("second", "tuber").add("third", "breem").build(),
				new TreeBuilder(reverseTriple)
						.add("first", "ank")
						.add("second", "reindeerkick")
						.add("third", "whatever")
						.build()
		).checkBrick(0, 2, 50).checkBrick(0, 3, 100).checkBrick(1, 2, 100).checkBrick(1, 3, 220);
	}

	@Test
	public void testPartiallyExpandConsecutiveLines() {
		new GeneralTestWizard(syntax, new TreeBuilder(threeLine2).build()).resize(50).resize(120).checkCourseCount(2);
	}

	@Test
	public void testSplitMultiCourseStackedAlignments() {
		new GeneralTestWizard(syntax, new TreeBuilder(threeLine).build()).resize(160).resize(170).checkCourseCount(4);
	}

	@Test
	public void testDynamicConcensusSplitAdjust() {
		final Function<Context, Visual> line2Visual =
				context -> Helper.rootArray(context.document).data.get(1).visual.parent().visual();
		final Atom pair1 = new TreeBuilder(pair).add("first", "12345678").add("second", "x").build();
		new GeneralTestWizard(syntax, pair1, new TreeBuilder(pair).add("first", "1").add("second", "y").build())
				.checkCourseCount(2)
				.checkBrick(0, 2, 80)
				.checkBrick(1, 2, 80)
				.run(context -> line2Visual
						.apply(context)
						.changeTags(context, new TagsChange().remove(new FreeTag("split"))))
				.checkCourseCount(1)
				.run(context -> line2Visual
						.apply(context)
						.changeTags(context, new TagsChange().add(new FreeTag("split"))))
				.run(context -> context.history.apply(context,
						new ChangePrimitiveAdd((ValuePrimitive) pair1.data.get("first"), 8, "9X")
				))
				.checkCourseCount(2)
				.checkBrick(0, 2, 100)
				.checkBrick(1, 2, 100);
	}

	@Test
	public void testDisabledConcensusSplit() {
		final Atom pairAtom1 = new TreeBuilder(pair).add("first", "gmippii").add("second", "mmm").build();
		final Atom lineAtom = new TreeBuilder(line).addArray("value", pairAtom1).build();
		new GeneralTestWizard(syntax, lineAtom)
				.checkCourseCount(1)
				.run(context -> context.history.apply(context,
						new ChangeArray((ValueArray) lineAtom.data.get("value"),
								1,
								0,
								ImmutableList.of(new TreeBuilder(pair).add("first", "mo").add("second", "oo").build(),
										new TreeBuilder(pair).add("first", "g").add("second", "q").build()
								)
						)
				))
				.run(context -> context.history.apply(context,
						new ChangePrimitiveAdd((ValuePrimitive) pairAtom1.data.get("first"), 7, "9X")
				))
				.run(context -> ((ValuePrimitive) pairAtom1.data.get("first")).visual.changeTags(context,
						new TagsChange().add(new FreeTag("split"))
				))
				.checkCourseCount(2);
	}

	@Test
	public void testDisabledConcensusSplit2() {
		final Atom pairAtom1 = new TreeBuilder(pair).add("first", "gmippii").add("second", "mmm").build();
		new GeneralTestWizard(syntax,
				new TreeBuilder(line)
						.addArray("value",
								pairAtom1,
								new TreeBuilder(pair).add("first", "mo").add("second", "oo").build()
						)
						.build()
		)
				.checkCourseCount(1)
				.run(context -> ((ValuePrimitive) pairAtom1.data.get("second")).visual.changeTags(context,
						new TagsChange().add(new FreeTag("split"))
				))
				.checkCourseCount(2);
	}
}
