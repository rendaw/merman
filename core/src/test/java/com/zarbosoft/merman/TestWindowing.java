package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.helper.*;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

public class TestWindowing {
	final public static FreeAtomType a0_0;
	final public static FreeAtomType a1_0;
	final public static FreeAtomType a2_0;
	final public static FreeAtomType a3_0;
	final public static FreeAtomType a4;
	final public static FreeAtomType a5;
	final public static FreeAtomType a0_1;
	final public static FreeAtomType a1_1;
	final public static FreeAtomType a2_1;
	final public static FreeAtomType a3_1;
	final public static FreeAtomType oneAtom;
	final public static FreeAtomType array;

	static {
		a0_0 = new TypeBuilder("a0_0").back(Helper.buildBackPrimitive("a0_0")).frontMark("0_0").build();
		a1_0 = new TypeBuilder("a1_0").back(Helper.buildBackPrimitive("a1_0")).frontMark("1_0").build();
		a2_0 = new TypeBuilder("a2_0").back(Helper.buildBackPrimitive("a2_0")).frontMark("2_0").build();
		a3_0 = new TypeBuilder("a3_0").back(Helper.buildBackPrimitive("a3_0")).frontMark("3_0").build();
		a4 = new TypeBuilder("a4").back(Helper.buildBackPrimitive("a4")).frontMark("4").build();
		a5 = new TypeBuilder("a5").back(Helper.buildBackPrimitive("a5")).frontMark("5").build();
		a0_1 = new TypeBuilder("a0_1").back(Helper.buildBackPrimitive("a0_1")).frontMark("0_1").build();
		a1_1 = new TypeBuilder("a1_1").back(Helper.buildBackPrimitive("a1_1")).frontMark("1_1").build();
		a2_1 = new TypeBuilder("a2_1").back(Helper.buildBackPrimitive("a2_1")).frontMark("2_1").build();
		a3_1 = new TypeBuilder("a3_1").back(Helper.buildBackPrimitive("a3_1")).frontMark("3_1").build();
		oneAtom = new TypeBuilder("oneAtom")
				.middleAtom("value", "any")
				.back(new BackRecordBuilder().add("stop", Helper.buildBackDataAtom("value")).build())
				.frontDataNode("value")
				.depthScore(1)
				.build();
		array = new TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value").build())
				.depthScore(1)
				.build();
	}

	public Syntax syntax(final boolean startWindowed) {
		final Syntax out = new SyntaxBuilder("any")
				.type(a0_0)
				.type(a1_0)
				.type(a2_0)
				.type(a3_0)
				.type(a4)
				.type(a5)
				.type(a0_1)
				.type(a1_1)
				.type(a2_1)
				.type(a3_1)
				.type(oneAtom)
				.type(array)
				.group(
						"any",
						new GroupBuilder()
								.type(a0_0)
								.type(a1_0)
								.type(a2_0)
								.type(a3_0)
								.type(a4)
								.type(a5)
								.type(a0_1)
								.type(a1_1)
								.type(a2_1)
								.type(a3_1)
								.type(oneAtom)
								.type(array)
								.build()
				)
				.style(new StyleBuilder().split(true).build())
				.build();
		out.startWindowed = startWindowed;
		out.ellipsizeThreshold = 3;
		return out;
	}

	public GeneralTestWizard start(final boolean startWindowed) {
		final Syntax syntax = syntax(startWindowed);
		return new GeneralTestWizard(
				syntax,
				new TreeBuilder(oneAtom)
						.add("value", new TreeBuilder(oneAtom).add("value", new TreeBuilder(a0_0).build()).build())
						.build(),
				new TreeBuilder(array)
						.addArray("value",
								new TreeBuilder(a1_0).build(),
								new TreeBuilder(array)
										.addArray("value",
												new TreeBuilder(a2_0).build(),
												new TreeBuilder(array).addArray("value",
														new TreeBuilder(a3_0).build(),
														new TreeBuilder(array)
																.addArray("value", new TreeBuilder(a4).build())
																.build(),
														new TreeBuilder(oneAtom)
																.add("value", new TreeBuilder(a5).build())
																.build(),
														new TreeBuilder(a3_1).build()
												).build(),
												new TreeBuilder(a2_1).build()
										)
										.build(),
								new TreeBuilder(a1_1).build()
						)
						.build(),
				new TreeBuilder(a0_1).build()
		);
	}

	@Test
	public void testInitialNoWindow() {
		int i = 0;
		start(false)
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testInitialWindow() {
		int i = 0;
		start(true)
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testWindowArray() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1");
	}

	@Test
	public void testWindowArrayUnselectable() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("2"))).parent.selectUp(context))
				.checkTextBrick(0, 0, "0_0")
				.act("window")
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testRewindowArray() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.run(context -> ((Atom) context.locateShort(new Path("1", "1"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1");
	}

	@Test
	public void testWindowAtom() {
		int i = 0;
		start(false)
				.run(context -> ((ValueAtom) context.locateShort(new Path("0", "stop"))).selectDown(context))
				.act("window")
				.checkTextBrick(i++, 0, "0_0");
	}

	@Test
	public void testWindowAtomUnselectable() {
		int i = 0;
		start(false)
				.run(context -> (
						(ValueAtom) context.locateShort(new Path("0", "stop", "stop"))
				).selectDown(context))
				.act("window")
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testWindowMaxDepth() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1", "1"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(i++, 0, "4");
	}

	@Test
	public void testWindowDown() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1"))).parent.selectUp(context))
				.act("window_down")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1");
	}

	@Test
	public void testWindowDownMaxDepth() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1", "1"))).parent.selectUp(context))
				.act("window")
				.act("window_down")
				.checkTextBrick(i++, 0, "4");
	}

	@Test
	public void testWindowUp() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1", "1"))).parent.selectUp(context))
				.act("window")
				.checkCourseCount(1)
				.checkTextBrick(0, 0, "4")
				.act("window_up")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1");
	}

	@Test
	public void testWindowUpRoot() {
		int i = 0;
		start(true)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 0, "1_0")
				.act("window_up")
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testWindowClear() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.act("window_clear")
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testWindowSelectArrayNoChange() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 0, "1_0")
				.run(context -> ((Atom) context.locateShort(new Path("1", "1"))).parent.selectUp(context))
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1");
	}

	@Test
	public void testWindowSelectArrayEllipsis() {
		int i = 0;
		start(false)
				.run(context -> ((Atom) context.locateShort(new Path("1"))).parent.selectUp(context))
				.act("window")
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1"))).parent.selectUp(context))
				.act("enter")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1");
	}

	@Test
	public void testWindowSelectArrayOutside() {
		int i = 0;
		start(true)
				.run(context -> ((Atom) context.locateShort(new Path("0"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 0, "0_0")
				.checkCourseCount(1)
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1", "1"))).parent.selectUp(context))
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "3_1")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1");
	}

	@Test
	public void testWindowSelectArrayOutsideRoot() {
		int i = 0;
		start(true)
				.run(context -> ((Atom) context.locateShort(new Path("0"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 0, "0_0")
				.checkCourseCount(1)
				.run(context -> ((Atom) context.locateShort(new Path("1", "1"))).parent.selectUp(context))
				.checkTextBrick(i++, 0, "0_0")
				.checkTextBrick(i++, 0, "1_0")
				.checkTextBrick(i++, 0, "2_0")
				.checkTextBrick(i++, 0, "...")
				.checkTextBrick(i++, 0, "2_1")
				.checkTextBrick(i++, 0, "1_1")
				.checkTextBrick(i++, 0, "0_1");
	}

	@Test
	public void testWindowSelectArrayAbove() {
		int i = 0;
		start(true)
				.run(context -> ((Atom) context.locateShort(new Path("1", "1", "1", "1"))).parent.selectUp(context))
				.act("window")
				.checkTextBrick(0, 0, "4")
				.checkCourseCount(1)
				.act("exit")
				.checkTextBrick(i++, 0, "3_0")
				.checkTextBrick(i++, 0, "4")
				.checkTextBrick(i++, 0, "5")
				.checkTextBrick(i++, 0, "3_1");
	}
}
