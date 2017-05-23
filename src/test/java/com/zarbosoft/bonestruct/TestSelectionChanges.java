package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.MiscSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.rendaw.common.Pair;
import org.junit.Test;

import static com.zarbosoft.bonestruct.helper.Helper.assertTreeEqual;
import static com.zarbosoft.bonestruct.helper.Helper.buildDoc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test changes to the selection when a change affects the selected nodes (or nearby nodes).
 */
public class TestSelectionChanges {

	private void innerTestTransform(
			final Syntax syntax,
			final Atom begin,
			final Path selectBegin,
			final Pair.Consumer<Context, Atom> transform,
			final Atom end,
			final Path selectEnd
	) {
		final Context context = buildDoc(syntax, begin);

		// Initial selection and double checking
		final Atom found = (Atom) context.locateLong(selectBegin);
		found.parent.selectUp(context);
		assertThat(context.selection.getPath(), equalTo(selectBegin));

		// Transform
		transform.accept(context, found);
		assertThat(context.document.rootArray.data.size(), equalTo(1));
		assertTreeEqual(context.document.rootArray.data.get(0), end);
		assertThat(context.selection.getPath(), equalTo(selectEnd));

		// Undo
		context.history.undo(context);
		assertThat(context.document.rootArray.data.size(), equalTo(1));
		assertTreeEqual(context.document.rootArray.data.get(0), begin);
		assertThat(context.selection.getPath(), equalTo(selectBegin));

		// Redo
		context.history.redo(context);
		assertThat(context.document.rootArray.data.size(), equalTo(1));
		assertTreeEqual(context.document.rootArray.data.get(0), end);
		assertThat(context.selection.getPath(), equalTo(selectEnd));
	}

	private void innerArrayTestTransform(
			final int beginSelectBegin,
			final int beginSelectEnd,
			final Pair.Consumer<Context, ValueArray> transform,
			final int endSelectBegin,
			final int endSelectEnd
	) {
		final Context context = buildDoc(MiscSyntax.syntax, new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
				new Helper.TreeBuilder(MiscSyntax.one).build(),
				new Helper.TreeBuilder(MiscSyntax.two).build(),
				new Helper.TreeBuilder(MiscSyntax.three).build(),
				new Helper.TreeBuilder(MiscSyntax.four).build(),
				new Helper.TreeBuilder(MiscSyntax.five).build()
		).build());

		final ValueArray value = (ValueArray) context.document.rootArray.data.get(0).data.get("value");
		final VisualArray visual = (VisualArray) value.visual;
		visual.select(context, true, beginSelectBegin, beginSelectEnd);
		final VisualArray.ArraySelection selection = visual.selection;

		// Transform
		transform.accept(context, value);
		assertThat(selection.beginIndex, equalTo(endSelectBegin));
		assertThat(selection.endIndex, equalTo(endSelectEnd));

		// Undo
		context.history.undo(context);
		assertThat(selection.beginIndex, equalTo(beginSelectBegin));
		assertThat(selection.endIndex, equalTo(beginSelectEnd));

		// Redo
		context.history.redo(context);
		assertThat(selection.beginIndex, equalTo(endSelectBegin));
		assertThat(selection.endIndex, equalTo(endSelectEnd));
	}

	@Test
	public void removeRootOnly() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.infinity).build(),
				new Path("0"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray(context.document.rootArray, 0, 1, ImmutableList.of())
				),
				MiscSyntax.syntax.gap.create(),
				new Path("0")
		);
	}

	@Test
	public void removeArrayOnly() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0"),
				(context, selected) -> {
					context.history.apply(context,
							new ChangeArray((ValueArray) selected.parent.value(), 0, 1, ImmutableList.of())
					);
				},
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value").build(),
				new Path("0")
		);
	}

	@Test
	public void removeArraySelectBefore() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "0"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 1, 1, ImmutableList.of())
				),
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0")
		);
	}

	@Test
	public void removeArraySelectFollowing() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 0, 1, ImmutableList.of())
				),
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0")
		);
	}

	@Test
	public void removeArraySelectWithin() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 1, 1, ImmutableList.of())
				),
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1")
		);
	}

	@Test
	public void removeArraySelectWithinNoneAfter() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 1, 1, ImmutableList.of())
				),
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0")
		);
	}

	@Test
	public void removeArraySelectDeep() {
		innerTestTransform(MiscSyntax.syntax, new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
				new Helper.TreeBuilder(MiscSyntax.infinity).build(),
				new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Helper.TreeBuilder(MiscSyntax.infinity).build()
		).build(), new Path("0", "1", "0"), (context, selected) -> {
			((Value) context.locateLong(new Path("0", "1"))).parent.atom().parent.delete(context);
		}, new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
				new Helper.TreeBuilder(MiscSyntax.infinity).build(),
				new Helper.TreeBuilder(MiscSyntax.infinity).build()
		).build(), new Path("0", "1"));
	}

	@Test
	public void addArrayAfter() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "0"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(),
								0,
								0,
								ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.infinity).build())
						)
				),
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1")
		);
	}

	@Test
	public void addArrayAfterEnd1() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(),
								1,
								0,
								ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.infinity).build())
						)
				),
				new Helper.TreeBuilder(MiscSyntax.array).addArray("value",
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build(),
						new Helper.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "2")
		);
	}

	@Test
	public void arrayBeginAtRemoveMultiple() {
		innerArrayTestTransform(0, 0, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 0, 2, ImmutableList.of()));
		}, 0, 0);
	}

	@Test
	public void arrayBeginRightBeforeRemoveMultiple() {
		innerArrayTestTransform(0, 0, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 1, 2, ImmutableList.of()));
		}, 0, 0);
	}

	@Test
	public void arrayBeginFarBeforeRemoveMultiple() {
		innerArrayTestTransform(0, 0, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 2, 2, ImmutableList.of()));
		}, 0, 0);
	}

	@Test
	public void arrayMidFarAfterRemoveMultiple() {
		innerArrayTestTransform(4, 4, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 0, 2, ImmutableList.of()));
		}, 2, 2);
	}

	@Test
	public void arrayMidRightAfterRemoveMultiple() {
		innerArrayTestTransform(3, 3, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 0, 2, ImmutableList.of()));
		}, 1, 1);
	}

	@Test
	public void arrayMidAtFirstRemoveMultiple() {
		innerArrayTestTransform(1, 1, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 1, 2, ImmutableList.of()));
		}, 1, 1);
	}

	@Test
	public void arrayMidAtSecondRemoveMultiple() {
		innerArrayTestTransform(2, 2, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 1, 2, ImmutableList.of()));
		}, 1, 1);
	}

	@Test
	public void arrayMidRightBeforeRemoveMultiple() {
		innerArrayTestTransform(1, 1, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 2, 2, ImmutableList.of()));
		}, 1, 1);
	}

	@Test
	public void arrayMidFarBeforeRemoveMultiple() {
		innerArrayTestTransform(1, 1, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 3, 2, ImmutableList.of()));
		}, 1, 1);
	}

	@Test
	public void arrayEndFarAfterRemoveMultiple() {
		innerArrayTestTransform(4, 4, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 0, 2, ImmutableList.of()));
		}, 2, 2);
	}

	@Test
	public void arrayEndRightAfterRemoveMultiple() {
		innerArrayTestTransform(4, 4, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 3, 2, ImmutableList.of()));
		}, 2, 2);
	}

	@Test
	public void arrayEndAtRemoveMultiple() {
		innerArrayTestTransform(3, 3, (context, value) -> {
			context.history.apply(context, new ChangeArray(value, 3, 2, ImmutableList.of()));
		}, 2, 2);
	}

	@Test
	public void arrayMidAtAddRemove() {
		innerArrayTestTransform(3, 3, (context, value) -> {
			context.history.apply(context,
					new ChangeArray(value, 3, 1, ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.one).build(),
							new Helper.TreeBuilder(MiscSyntax.one).build()
					))
			);
		}, 4, 4);
	}

	@Test
	public void arrayEndAtAddRemove() {
		innerArrayTestTransform(4, 4, (context, value) -> {
			context.history.apply(context,
					new ChangeArray(value, 4, 1, ImmutableList.of(new Helper.TreeBuilder(MiscSyntax.one).build(),
							new Helper.TreeBuilder(MiscSyntax.one).build()
					))
			);
		}, 5, 5);
	}

	@Test
	public void removeNode() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "value"),
				(context, selected) -> {
					context.history.apply(context,
							new ChangeNodeSet((ValueAtom) selected.parent.value(), MiscSyntax.syntax.gap.create())
					);
				},
				new Helper.TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				new Path("0", "value")
		);
	}

	@Test
	public void removeNodeSelectDeep() {
		innerTestTransform(MiscSyntax.syntax,
				new Helper.TreeBuilder(MiscSyntax.snooze).add("value",
						new Helper.TreeBuilder(MiscSyntax.array)
								.addArray("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
								.build()
				).build(),
				new Path("0", "value", "0"),
				(context, selected) -> {
					((ValueArray) context.locateLong(new Path("0", "value"))).parent.atom().parent.delete(context);
				},
				new Helper.TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				new Path("0", "value")
		);
	}
}
