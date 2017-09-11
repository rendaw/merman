package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

public class TestLayoutArray {

	@Test
	public void testStatic() {
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.array)
						.addArray("value",
								new TreeBuilder(MiscSyntax.one).build(),
								new TreeBuilder(MiscSyntax.one).build()
						)
						.build()
		)
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 2, ", ")
				.checkTextBrick(0, 3, "one")
				.checkTextBrick(0, 4, "]");
	}

	@Test
	public void testStaticNestedArray() {
		int index = 0;
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.array)
						.addArray("value", new TreeBuilder(MiscSyntax.array).addArray("value").build())
						.build()
		)
				.checkTextBrick(0, index++, "[")
				.checkTextBrick(0, index++, "[")
				.checkSpaceBrick(0, index++)
				.checkTextBrick(0, index++, "]")
				.checkTextBrick(0, index++, "]");
	}

	@Test
	public void testDynamicAddFirst() {
		final Atom arrayAtom = new TreeBuilder(MiscSyntax.array).addArray("value").build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 0, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))
				))
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 2, "]");
	}

	@Test
	public void testDynamicAddSecond() {
		final Atom arrayAtom =
				new TreeBuilder(MiscSyntax.array).addArray("value", new TreeBuilder(MiscSyntax.one).build()).build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 0, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))
				))
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 2, ", ")
				.checkTextBrick(0, 3, "one")
				.checkTextBrick(0, 4, "]");
	}

	@Test
	public void testDynamicAddSecondAfter() {
		final Atom arrayAtom =
				new TreeBuilder(MiscSyntax.array).addArray("value", new TreeBuilder(MiscSyntax.one).build()).build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 1, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))
				))
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 2, ", ")
				.checkTextBrick(0, 3, "one")
				.checkTextBrick(0, 4, "]");
	}

	@Test
	public void testDynamicAddSecondAfterArray() {
		final Atom arrayAtom = new TreeBuilder(MiscSyntax.array)
				.addArray("value", new TreeBuilder(MiscSyntax.array).addArray("value").build())
				.build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		final int index = 0;
		int index2 = 0;
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context,
						new ChangeArray(array, 1, 0, ImmutableList.of(new TreeBuilder(MiscSyntax.one).build()))
				))
				.checkTextBrick(0, index2++, "[")
				.checkTextBrick(0, index2++, "[")
				.checkSpaceBrick(0, index2++)
				.checkTextBrick(0, index2++, "]")
				.checkTextBrick(0, index2++, ", ")
				.checkTextBrick(0, index2++, "one")
				.checkTextBrick(0, index2++, "]");
	}

	@Test
	public void testDynamicDeleteFirstPart() {
		final Atom arrayAtom = new TreeBuilder(MiscSyntax.array)
				.addArray("value", new TreeBuilder(MiscSyntax.one).build(), new TreeBuilder(MiscSyntax.one).build())
				.build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context, new ChangeArray(array, 0, 1, ImmutableList.of())))
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 2, "]");
	}

	@Test
	public void testDynamicDeleteSecondPart() {
		final Atom arrayAtom = new TreeBuilder(MiscSyntax.array)
				.addArray("value", new TreeBuilder(MiscSyntax.one).build(), new TreeBuilder(MiscSyntax.one).build())
				.build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context, new ChangeArray(array, 1, 1, ImmutableList.of())))
				.checkTextBrick(0, 0, "[")
				.checkTextBrick(0, 1, "one")
				.checkTextBrick(0, 2, "]");
	}

	@Test
	public void testDynamicDeleteLast() {
		final Atom arrayAtom =
				new TreeBuilder(MiscSyntax.array).addArray("value", new TreeBuilder(MiscSyntax.one).build()).build();
		final ValueArray array = (ValueArray) arrayAtom.data.get("value");
		new GeneralTestWizard(MiscSyntax.syntax, arrayAtom)
				.run(context -> context.history.apply(context, new ChangeArray(array, 0, 1, ImmutableList.of())))
				.checkTextBrick(0, 0, "[")
				.checkSpaceBrick(0, 1)
				.checkTextBrick(0, 2, "]");
	}

	@Test
	public void testDynamicGapDeselectLast() {
		new GeneralTestWizard(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.array).addArray("value").build())
				.act("enter")
				.act("exit")
				.checkTextBrick(0, 0, "[")
				.checkSpaceBrick(0, 1)
				.checkTextBrick(0, 2, "]");
	}
}
