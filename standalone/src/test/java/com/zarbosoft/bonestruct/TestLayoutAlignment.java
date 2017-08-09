package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.bonestruct.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.editor.visual.tags.TypeTag;
import com.zarbosoft.bonestruct.helper.*;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

public class TestLayoutAlignment {
	final public static FreeAtomType relative;
	final public static FreeAtomType absolute;
	final public static FreeAtomType array;
	final public static FreeAtomType compactArray;
	final public static FreeAtomType line;
	final public static FreeAtomType pair;
	final public static FreeAtomType triple;
	final public static Syntax syntax;

	static {
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
		syntax = new SyntaxBuilder("any")
				.type(absolute)
				.type(relative)
				.type(array)
				.type(compactArray)
				.type(line)
				.type(pair)
				.type(triple)
				.group("any",
						new GroupBuilder()
								.type(absolute)
								.type(relative)
								.type(array)
								.type(compactArray)
								.type(pair)
								.type(triple)
								.type(line)
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
				.style(new StyleBuilder().tag(new FreeTag("concensus2")).alignment("concensus2").build())
				.style(new StyleBuilder().tag(new TypeTag("relative")).alignment("relative").build())
				.build();
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
				.run(context -> context.history.apply(context, new ChangeArray(context.document.rootArray,
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
						new ChangeArray(context.document.rootArray, 1, 1, ImmutableList.of())
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
		new GeneralTestWizard(syntax, new TreeBuilder(compactArray).addArray("value",
				new TreeBuilder(pair).add("first", "a").add("second", "b").build(),
				new TreeBuilder(pair).add("first", "cccc").add("second", "d").build()
		).build()).resize(60).checkTextBrick(1, 1, "a").checkTextBrick(2, 1, "cccc").resize(70).checkCourseCount(1);
	}
}
