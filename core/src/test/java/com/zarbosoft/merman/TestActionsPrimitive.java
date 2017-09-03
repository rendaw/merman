package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.helper.GeneralTestWizard;
import com.zarbosoft.merman.helper.Helper;
import com.zarbosoft.merman.helper.MiscSyntax;
import com.zarbosoft.merman.helper.TreeBuilder;
import org.junit.Test;

import static com.zarbosoft.merman.helper.Helper.assertTreeEqual;
import static com.zarbosoft.merman.helper.Helper.buildDoc;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class TestActionsPrimitive {
	public static Context build(final String string) {
		final Context context =
				buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.quoted).add("value", string).build());
		Helper.rootArray(context.document).data.get(0).data.get("value").selectDown(context);
		return context;
	}

	public static Context buildFive() {
		return build("12345");
	}

	public static VisualPrimitive visual(final Context context) {
		return (VisualPrimitive) context.selection.getVisual();
	}

	public static void assertSelection(final Context context, final int begin, final int end) {
		final VisualPrimitive.PrimitiveSelection selection = (VisualPrimitive.PrimitiveSelection) context.selection;
		assertThat(selection.range.beginOffset, equalTo(begin));
		assertThat(selection.range.endOffset, equalTo(end));
	}

	@Test
	public void testExit() {
		final Context context = buildFive();
		assertThat(context.selection.getVisual(), instanceOf(VisualPrimitive.class));
		Helper.act(context, "exit");
		assertNotNull(((VisualArray) Helper.rootArray(context.document).visual).selection);
	}

	@Test
	public void testNext() {
		final Atom target = new TreeBuilder(MiscSyntax.doubleQuoted).add("first", "").add("second", "").build();
		final ValuePrimitive value = (ValuePrimitive) target.data.get("first");
		new GeneralTestWizard(MiscSyntax.syntax, target)
				.run(context -> value.selectDown(context))
				.act("next")
				.run(context -> assertThat(context.selection.getPath().toList(),
						equalTo(ImmutableList.of("0", "second", "0"))
				));
	}

	@Test
	public void testPrevious() {
		final Atom target = new TreeBuilder(MiscSyntax.doubleQuoted).add("first", "").add("second", "").build();
		final ValuePrimitive value = (ValuePrimitive) target.data.get("second");
		new GeneralTestWizard(MiscSyntax.syntax, target)
				.run(context -> value.selectDown(context))
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
		assertSelection(context, 3, 3);
	}

	@Test
	public void testNextEOL() {
		final Context context = build("1\n2");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "next_element");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testNextDeselect() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 2);
		Helper.act(context, "next_element");
		assertSelection(context, 3, 3);
	}

	@Test
	public void testNextEnd() {
		final Context context = buildFive();
		visual(context).select(context, true, 5, 5);
		Helper.act(context, "next_element");
		assertSelection(context, 5, 5);
	}

	@Test
	public void testNextDeselectEnd() {
		final Context context = buildFive();
		visual(context).select(context, true, 4, 5);
		Helper.act(context, "next_element");
		assertSelection(context, 5, 5);
	}

	@Test
	public void testPreviousElement() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "previous_element");
		assertSelection(context, 1, 1);
	}

	@Test
	public void testPreviousBOL() {
		final Context context = build("a\n2");
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "previous_element");
		assertSelection(context, 1, 1);
	}

	@Test
	public void testPreviousDeselect() {
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
		assertSelection(context, 0, 0);
	}

	@Test
	public void testPreviousDeselectStart() {
		final Context context = buildFive();
		visual(context).select(context, true, 0, 1);
		Helper.act(context, "previous_element");
		assertSelection(context, 0, 0);
	}

	@Test
	public void testNextLineLast() {
		final Context context = build("12");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "next_line");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testNextLineStart() {
		final Context context = build("12\n34");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "next_line");
		assertSelection(context, 3, 3);
	}

	@Test
	public void testNextLineMid() {
		final Context context = build("12\n34");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "next_line");
		assertSelection(context, 4, 4);
	}

	@Test
	public void testNextLineEnd() {
		final Context context = build("12\n34");
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "next_line");
		assertSelection(context, 5, 5);
	}

	@Test
	public void testNextLineLimit() {
		final Context context = build("12\n3");
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "next_line");
		assertSelection(context, 4, 4);
	}

	@Test
	public void testPreviousLineFirst() {
		final Context context = build("12");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "previous_line");
		assertSelection(context, 0, 0);
	}

	@Test
	public void testPreviousLineStart() {
		final Context context = build("12\n34");
		visual(context).select(context, true, 3, 3);
		Helper.act(context, "previous_line");
		assertSelection(context, 0, 0);
	}

	@Test
	public void testPreviousLineMid() {
		final Context context = build("12\n34");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "previous_line");
		assertSelection(context, 1, 1);
	}

	@Test
	public void testPreviousLineEnd() {
		final Context context = build("12\n34");
		visual(context).select(context, true, 5, 5);
		Helper.act(context, "previous_line");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testPreviousLineLimit() {
		final Context context = build("1\n34");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "previous_line");
		assertSelection(context, 1, 1);
	}

	@Test
	public void testLineBegin() {
		final Context context = build("01\n23\n45");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "line_begin");
		assertSelection(context, 3, 3);
	}

	@Test
	public void testLineEnd() {
		final Context context = build("01\n23\n45");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "line_end");
		assertSelection(context, 5, 5);
	}

	@Test
	public void testLastLineEnd() {
		final Context context = build("01");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "line_end");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testFirstLineBegin() {
		final Context context = build("01");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "line_begin");
		assertSelection(context, 0, 0);
	}

	@Test
	public void testNextWordMid() {
		final Context context = build("the dog");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "next_word");
		assertSelection(context, 3, 3);
	}

	@Test
	public void testNextWordBoundary() {
		final Context context = build("the dog");
		visual(context).select(context, true, 3, 3);
		Helper.act(context, "next_word");
		assertSelection(context, 4, 4);
	}

	@Test
	public void testPreviousWordMid() {
		final Context context = build("the dog");
		visual(context).select(context, true, 5, 5);
		Helper.act(context, "previous_word");
		assertSelection(context, 4, 4);
	}

	@Test
	public void testPreviousWordBoundary() {
		final Context context = build("the dog");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "previous_word");
		assertSelection(context, 3, 3);
	}

	@Test
	public void testGatherNext() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "gather_next");
		assertSelection(context, 2, 3);
	}

	@Test
	public void testGatherNextEnd() {
		final Context context = buildFive();
		visual(context).select(context, true, 5, 5);
		Helper.act(context, "gather_next");
		assertSelection(context, 5, 5);
	}

	@Test
	public void testGatherNextWord() {
		final Context context = build("dog hat chair");
		visual(context).select(context, true, 3, 4);
		Helper.act(context, "gather_next_word");
		assertSelection(context, 3, 7);
	}

	@Test
	public void testGatherNextLineEnd() {
		final Context context = build("dog hat\n chair");
		visual(context).select(context, true, 3, 3);
		Helper.act(context, "gather_next_line_end");
		assertSelection(context, 3, 7);
	}

	@Test
	public void testGatherNextLine() {
		final Context context = build("dog hat\n chair");
		visual(context).select(context, true, 3, 3);
		Helper.act(context, "gather_next_line");
		assertSelection(context, 3, 11);
	}

	@Test
	public void testGatherPrevious() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "gather_previous");
		assertSelection(context, 1, 2);
	}

	@Test
	public void testGatherPreviousWord() {
		final Context context = build("chair hat pan");
		visual(context).select(context, true, 9, 9);
		Helper.act(context, "gather_previous_word");
		assertSelection(context, 6, 9);
	}

	@Test
	public void testGatherPreviousLineStart() {
		final Context context = build("chair\nhat pan");
		visual(context).select(context, true, 9, 9);
		Helper.act(context, "gather_previous_line_start");
		assertSelection(context, 6, 9);
	}

	@Test
	public void testGatherPreviousLine() {
		final Context context = build("chair\nhat pan");
		visual(context).select(context, true, 9, 9);
		Helper.act(context, "gather_previous_line");
		assertSelection(context, 3, 9);
	}

	@Test
	public void testGatherPreviousStart() {
		final Context context = buildFive();
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "gather_previous");
		assertSelection(context, 0, 0);
	}

	@Test
	public void testReleaseNext() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 3);
		Helper.act(context, "release_next");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testReleaseNextMinimum() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "release_next");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testReleaseNextWord() {
		final Context context = build("kettle rubarb");
		visual(context).select(context, true, 6, 13);
		Helper.act(context, "release_next_word");
		assertSelection(context, 6, 7);
	}

	@Test
	public void testReleaseNextLineEnd() {
		final Context context = build("one\ntwo three");
		visual(context).select(context, true, 1, 8);
		Helper.act(context, "release_next_line_end");
		assertSelection(context, 1, 4);
	}

	@Test
	public void testReleaseNextLine() {
		final Context context = build("one\ntwo three");
		visual(context).select(context, true, 1, 7);
		Helper.act(context, "release_next_line");
		assertSelection(context, 1, 3);
	}

	@Test
	public void testReleasePrevious() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 2);
		Helper.act(context, "release_previous");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testReleasePreviousMinimum() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "release_previous");
		assertSelection(context, 2, 2);
	}

	@Test
	public void testReleasePreviousWord() {
		final Context context = build("truck frypan");
		visual(context).select(context, true, 0, 10);
		Helper.act(context, "release_previous_word");
		assertSelection(context, 5, 10);
	}

	@Test
	public void testReleasePreviousLineStart() {
		final Context context = build("no\nyes");
		visual(context).select(context, true, 0, 5);
		Helper.act(context, "release_previous_line_start");
		assertSelection(context, 2, 5);
	}

	@Test
	public void testReleasePreviousLine() {
		final Context context = build("no\nyes");
		visual(context).select(context, true, 0, 5);
		Helper.act(context, "release_previous_line");
		assertSelection(context, 3, 5);
	}

	@Test
	public void testSplitSingleStart() {
		final Context context = build("ab");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "\nab").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testSplitSingleMid() {
		final Context context = build("ab");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "a\nb").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 2, 2);
	}

	@Test
	public void testSplitSingleEnd() {
		final Context context = build("ab");
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ab\n").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 3, 3);
	}

	@Test
	public void testSplitMultipleStart() {
		final Context context = build("1\nab\n2");
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1\n\nab\n2").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 3, 3);
	}

	@Test
	public void testSplitMultipleMid() {
		final Context context = build("1\nab\n2");
		visual(context).select(context, true, 3, 3);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1\na\nb\n2").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 4, 4);
	}

	@Test
	public void testSplitMultipleEnd() {
		final Context context = build("1\nab\n2");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1\nab\n\n2").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 5, 5);
	}

	@Test
	public void testSplitEmpty() {
		final Context context = build("");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "\n").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testSplitRange() {
		final Context context = build("abcd");
		visual(context).select(context, true, 1, 3);
		Helper.act(context, "split");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "a\nd").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 2, 2);
	}

	@Test
	public void testJoinEmpty() {
		final Context context = build("");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "join");
		assertSelection(context, 0, 0);
	}

	@Test
	public void testJoinMinimal() {
		final Context context = build("\n");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "join");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 0, 0);
	}

	@Test
	public void testJoin() {
		final Context context = build("a\nb");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "join");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ab").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testJoinRange() {
		final Context context = build("ab\nc\nde");
		visual(context).select(context, true, 1, 6);
		Helper.act(context, "join");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "abcde").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 4);
	}

	@Test
	public void testDeletePrevious() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "delete_previous");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1345").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteBOL() {
		final Context context = build("a\nb");
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "delete_previous");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ab").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteBOF() {
		final Context context = build("a");
		visual(context).select(context, true, 0, 0);
		Helper.act(context, "delete_previous");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "a").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 0, 0);
	}

	@Test
	public void testDeletePreviousRange() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 2);
		Helper.act(context, "delete_previous");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1345").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeletePreviousRangeLines() {
		final Context context = build("ab\ncd");
		visual(context).select(context, true, 1, 4);
		Helper.act(context, "delete_previous");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ad").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteNext() {
		final Context context = buildFive();
		visual(context).select(context, true, 2, 2);
		Helper.act(context, "delete_next");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1245").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 2, 2);
	}

	@Test
	public void testDeleteEOL() {
		final Context context = build("a\nb");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "delete_next");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ab").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteEOF() {
		final Context context = build("a");
		visual(context).select(context, true, 1, 1);
		Helper.act(context, "delete_next");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "a").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteNextRange() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 2);
		Helper.act(context, "delete_next");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1345").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteNextRangeLines() {
		final Context context = build("ab\ncd");
		visual(context).select(context, true, 1, 4);
		Helper.act(context, "delete_next");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ad").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 1, 1);
	}

	@Test
	public void testDeleteNextLongRangeLines() {
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.quoted).add("value", "ab\ncognate\nefg").build()
		)
				.run(context -> Helper.rootArray(context.document).data.get(0).data.get("value").selectDown(context))
				.run(context -> visual(context).select(context, true, 1, 13))
				.act("delete_next")
				.checkTextBrick(0, 1, "ag")
				.checkCourseCount(1)
				.checkArrayTree(new TreeBuilder(MiscSyntax.quoted).add("value", "ag").build())
				.run(context -> assertSelection(context, 1, 1));
	}

	@Test
	public void testCopyPasteSingle() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 3);
		Helper.act(context, "copy");
		visual(context).select(context, true, 4, 4);
		Helper.act(context, "paste");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1234235").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 6, 6);
	}

	@Test
	public void testCopyPasteRange() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 3);
		Helper.act(context, "copy");
		visual(context).select(context, true, 4, 5);
		Helper.act(context, "paste");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "123423").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 6, 6);
	}

	@Test
	public void testCutPaste() {
		final Context context = buildFive();
		visual(context).select(context, true, 1, 3);
		Helper.act(context, "cut");
		assertSelection(context, 1, 1);
		visual(context).select(context, true, 2, 3);
		Helper.act(context, "paste");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.quoted).add("value", "1423").build(),
				Helper.rootArray(context.document)
		);
		assertSelection(context, 4, 4);
	}
}
