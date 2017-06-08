package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import com.zarbosoft.bonestruct.helper.GeneralTestWizard;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.MiscSyntax;
import com.zarbosoft.bonestruct.helper.TreeBuilder;
import org.junit.Test;

import static com.zarbosoft.bonestruct.helper.Helper.assertTreeEqual;
import static com.zarbosoft.bonestruct.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsArray {

	public Context build(final Atom... atoms) {
		final Context context =
				buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.array).addArray("value", atoms).build());
		((ValueArray) context.document.rootArray.data.get(0).data.get("value")).selectDown(context);
		return context;
	}

	public Context buildFive() {
		return build(new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		);
	}

	public static VisualArray visual(final Context context) {
		return (VisualArray) context.selection.getVisual().parent().visual();
	}

	public static void assertSelection(final Context context, final int begin, final int end) {
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(begin));
		assertThat(selection.endIndex, equalTo(end));
	}

	@Test
	public void testEnter() {
		final Context context = build(new TreeBuilder(MiscSyntax.snooze)
				.add("value", new TreeBuilder(MiscSyntax.infinity).build())
				.build());
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "enter");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0", "value")));
	}

	@Test
	public void testExit() {
		final Context context = build(new TreeBuilder(MiscSyntax.snooze)
				.add("value", new TreeBuilder(MiscSyntax.infinity).build())
				.build());
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "exit");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0")));
	}

	@Test
	public void testNext() {
		final Atom target = new TreeBuilder(MiscSyntax.one).build();
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.doubleArray)
						.addArray("first", target)
						.addArray("second", new TreeBuilder(MiscSyntax.one).build())
						.build()
		)
				.run(context -> target.parent.selectUp(context))
				.act("next")
				.run(context -> assertThat(context.selection.getPath().toList(),
						equalTo(ImmutableList.of("0", "second", "0"))
				));
	}

	@Test
	public void testPrevious() {
		final Atom target = new TreeBuilder(MiscSyntax.one).build();
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.doubleArray)
						.addArray("first", new TreeBuilder(MiscSyntax.one).build())
						.addArray("second", target)
						.build()
		)
				.run(context -> target.parent.selectUp(context))
				.act("previous")
				.run(context -> assertThat(context.selection.getPath().toList(),
						equalTo(ImmutableList.of("0", "first", "0"))
				));
	}

	@Test
	public void testNextElement() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "next_element");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "3")));
	}

	@Test
	public void testNextRange() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 3);
		Helper.act(context, "next_element");
		assertSelection(context, 4, 4);
	}

	@Test
	public void testNextEnd() {
		final Context context = buildFive();
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "next_element");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "4")));
	}

	@Test
	public void testPreviousElement() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "previous_element");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1")));
	}

	@Test
	public void testPreviousRange() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 3);
		Helper.act(context, "previous_element");
		assertSelection(context, 1, 1);
	}

	@Test
	public void testPreviousStart() {
		final Context context = buildFive();
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "previous_element");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
	}

	@Test
	public void testGatherNext() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "gather_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(3));
	}

	@Test
	public void testGatherNextEnd() {
		final Context context = buildFive();
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "gather_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(4));
		assertThat(selection.endIndex, equalTo(4));
	}

	@Test
	public void testGatherPrevious() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "gather_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(1));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testGatherPreviousStart() {
		final Context context = buildFive();
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "gather_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(0));
		assertThat(selection.endIndex, equalTo(0));
	}

	@Test
	public void testReleaseNext() {
		final Context context = buildFive();
		((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual).select(context,
				true,
				2,
				3
		);
		Helper.act(context, "release_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testReleaseNextMinimum() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "release_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testReleasePrevious() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "release_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testReleasePreviousMinimum() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "release_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testDelete() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "delete");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1")));
	}

	@Test
	public void testInsertBefore() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "insert_before");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				MiscSyntax.syntax.gap.create(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1", "0")));
	}

	@Test
	public void testInsertAfter() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "insert_after");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				MiscSyntax.syntax.gap.create(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "3", "0")));
	}

	@Test
	public void testMoveBefore() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "move_before");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(0));
		assertThat(selection.endIndex, equalTo(1));
	}

	@Test
	public void testMoveBeforeStart() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				0,
				1
		);
		Helper.act(context, "move_before");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(0));
		assertThat(selection.endIndex, equalTo(1));
	}

	@Test
	public void testMoveAfter() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "move_after");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(3));
	}

	@Test
	public void testMoveAfterEnd() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				3,
				4
		);
		Helper.act(context, "move_after");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(3));
		assertThat(selection.endIndex, equalTo(4));
	}

	@Test
	public void testCopyPaste() {
		final Context context = buildFive();
		final VisualArray visual =
				((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual);
		visual.select(context, true, 1, 2);
		Helper.act(context, "copy");
		visual.select(context, true, 4, 4);
		Helper.act(context, "paste");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build()
		).build(), context.document.rootArray);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(5));
		assertThat(selection.endIndex, equalTo(5));
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "5")));
	}

	@Test
	public void testCutPaste() {
		final Context context = buildFive();
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				1,
				2
		);
		Helper.act(context, "cut");
		{
			final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
			assertThat(selection.beginIndex, equalTo(1));
			assertThat(selection.endIndex, equalTo(1));
		}
		(((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual)).select(context,
				true,
				2,
				2
		);
		Helper.act(context, "paste");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.two).build(),
				new TreeBuilder(MiscSyntax.three).build()
		).build(), context.document.rootArray);
		{
			final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
			assertThat(selection.beginIndex, equalTo(3));
			assertThat(selection.endIndex, equalTo(3));
		}
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "3")));
	}

	@Test
	public void testPrefix() {
		final Context context = buildFive();
		final VisualArray visual =
				((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual);
		visual.select(context, true, 1, 1);
		Helper.act(context, "prefix");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				MiscSyntax.syntax.prefixGap.create(new TreeBuilder(MiscSyntax.two).build()),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1", "gap", "0")));
	}

	@Test
	public void testSuffix() {
		final Context context = buildFive();
		final VisualArray visual =
				((VisualArray) ((ValueArray) ((Atom) context.locateShort(new Path("0"))).data.get("value")).visual);
		visual.select(context, true, 1, 1);
		Helper.act(context, "suffix");
		assertTreeEqual(context, new TreeBuilder(MiscSyntax.array).addArray("value",
				new TreeBuilder(MiscSyntax.one).build(),
				MiscSyntax.syntax.suffixGap.create(false, new TreeBuilder(MiscSyntax.two).build()),
				new TreeBuilder(MiscSyntax.three).build(),
				new TreeBuilder(MiscSyntax.four).build(),
				new TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.rootArray);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1", "gap", "0")));
	}
}
