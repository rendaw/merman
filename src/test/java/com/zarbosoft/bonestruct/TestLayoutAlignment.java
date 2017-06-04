package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.TypeTag;
import com.zarbosoft.bonestruct.helper.*;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

public class TestLayoutAlignment {
	final public static FreeAtomType relative;
	final public static FreeAtomType absolute;
	final public static FreeAtomType array;
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
				.type(pair)
				.type(triple)
				.group("any",
						new GroupBuilder().type(absolute).type(relative).type(array).type(pair).type(triple).build()
				)
				.absoluteAlignment("absolute", 7)
				.relativeAlignment("relative", 3)
				.concensusAlignment("concensus1")
				.concensusAlignment("concensus2")
				.addRootFrontPrefix(new FrontSpaceBuilder().tag("split").build())
				.style(new StyleBuilder().tag(new FreeTag("split")).split(true).build())
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
}
