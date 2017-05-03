package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import org.junit.Test;

import static com.zarbosoft.bonestruct.Helper.assertTreeEqual;
import static com.zarbosoft.bonestruct.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsArray {

	public Context build(final Node... nodes) {
		final Context context =
				buildDoc(MiscSyntax.syntax, new Helper.TreeBuilder(MiscSyntax.array).addArray("value", nodes).build());
		((ValueArray) context.document.top.get().get(0).data.get("value")).visual.select(context);
		return context;
	}

	public Context buildFive() {
		return build(
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		);
	}

	public static VisualArray visual(final Context context) {
		return (VisualArray) context.selection.getVisual().parent().getTarget();
	}

	public static void assertSelection(final Context context, final int begin, final int end) {
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(begin));
		assertThat(selection.endIndex, equalTo(end));
	}

	@Test
	public void testEnter() {
		final Context context = build(new Helper.TreeBuilder(MiscSyntax.snooze)
				.add("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
				.build());
		visual(context).select(context, 0, 0);
		Helper.act(context, "enter");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0", "value")));
	}

	@Test
	public void testExit() {
		final Context context = build(new Helper.TreeBuilder(MiscSyntax.snooze)
				.add("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
				.build());
		visual(context).select(context, 0, 0);
		Helper.act(context, "exit");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0")));
	}

	@Test
	public void testNext() {
		final Context context = buildFive();
		visual(context).select(context, 2, 2);
		Helper.act(context, "next");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "3")));
	}

	@Test
	public void testNextRange() {
		final Context context = buildFive();
		visual(context).select(context, 2, 3);
		Helper.act(context, "next");
		assertSelection(context, 4, 4);
	}

	@Test
	public void testNextEnd() {
		final Context context = buildFive();
		visual(context).select(context, 4, 4);
		Helper.act(context, "next");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "4")));
	}

	@Test
	public void testPrevious() {
		final Context context = buildFive();
		visual(context).select(context, 2, 2);
		Helper.act(context, "previous");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1")));
	}

	@Test
	public void testPreviousRange() {
		final Context context = buildFive();
		visual(context).select(context, 2, 3);
		Helper.act(context, "previous");
		assertSelection(context, 1, 1);
	}

	@Test
	public void testPreviousStart() {
		final Context context = buildFive();
		visual(context).select(context, 0, 0);
		Helper.act(context, "previous");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
	}

	@Test
	public void testGatherNext() {
		final Context context = buildFive();
		visual(context).select(context, 2, 2);
		Helper.act(context, "gather_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(3));
	}

	@Test
	public void testGatherNextEnd() {
		final Context context = buildFive();
		visual(context).select(context, 4, 4);
		Helper.act(context, "gather_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(4));
		assertThat(selection.endIndex, equalTo(4));
	}

	@Test
	public void testGatherPrevious() {
		final Context context = buildFive();
		visual(context).select(context, 2, 2);
		Helper.act(context, "gather_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(1));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testGatherPreviousStart() {
		final Context context = buildFive();
		visual(context).select(context, 0, 0);
		Helper.act(context, "gather_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(0));
		assertThat(selection.endIndex, equalTo(0));
	}

	@Test
	public void testReleaseNext() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 2, 3);
		Helper.act(context, "release_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testReleaseNextMinimum() {
		final Context context = buildFive();
		visual(context).select(context, 2, 2);
		Helper.act(context, "release_next");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testReleasePrevious() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 1, 2);
		Helper.act(context, "release_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testReleasePreviousMinimum() {
		final Context context = buildFive();
		visual(context).select(context, 2, 2);
		Helper.act(context, "release_previous");
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(2));
	}

	@Test
	public void testDelete() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 1, 2);
		Helper.act(context, "delete");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.top);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "1")));
	}

	@Test
	public void testMoveBefore() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 1, 2);
		Helper.act(context, "move_before");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.top);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(0));
		assertThat(selection.endIndex, equalTo(1));
	}

	@Test
	public void testMoveBeforeStart() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 0, 1);
		Helper.act(context, "move_before");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.top);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(0));
		assertThat(selection.endIndex, equalTo(1));
	}

	@Test
	public void testMoveAfter() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 1, 2);
		Helper.act(context, "move_after");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.top);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(2));
		assertThat(selection.endIndex, equalTo(3));
	}

	@Test
	public void testMoveAfterEnd() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 3, 4);
		Helper.act(context, "move_after");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		).build(), context.document.top);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(3));
		assertThat(selection.endIndex, equalTo(4));
	}

	@Test
	public void testCopyPaste() {
		final Context context = buildFive();
		final VisualArray visual =
				(VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual();
		visual.select(context, 1, 2);
		Helper.act(context, "copy");
		visual.select(context, 4, 4);
		Helper.act(context, "paste");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build()
		).build(), context.document.top);
		final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
		assertThat(selection.beginIndex, equalTo(5));
		assertThat(selection.endIndex, equalTo(5));
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "5")));
	}

	@Test
	public void testCutPaste() {
		final Context context = buildFive();
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 1, 2);
		Helper.act(context, "cut");
		{
			final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
			assertThat(selection.beginIndex, equalTo(1));
			assertThat(selection.endIndex, equalTo(1));
		}
		((VisualArray) ((Node) context.locateShort(new Path("0"))).data.get("value").getVisual()).select(context, 2, 2);
		Helper.act(context, "paste");
		assertTreeEqual(context, new Helper.TreeBuilder(MiscSyntax.array).addArray(
				"value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build()
		).build(), context.document.top);
		{
			final VisualArray.ArraySelection selection = (VisualArray.ArraySelection) context.selection;
			assertThat(selection.beginIndex, equalTo(3));
			assertThat(selection.endIndex, equalTo(3));
		}
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "3")));
	}
}
