package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.rendaw.common.Pair;
import org.junit.Test;

import static com.zarbosoft.bonestruct.Builders.assertTreeEqual;
import static com.zarbosoft.bonestruct.Builders.buildDoc;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test changes to the selection when a change affects the selected nodes (or nearby nodes).
 */
public class TestSelectionChanges {

	private void innerTestTransform(
			final Syntax syntax,
			final Node begin,
			final Path selectBegin,
			final Pair.Consumer<Context, Node> transform,
			final Node end,
			final Path selectEnd
	) {
		final Context context = buildDoc(syntax, begin);

		// Initial selection and double checking
		final Node found = (Node) context.locateLong(selectBegin);
		found.getVisual().parent().selectUp(context);
		assertThat(context.selection.getPath(), equalTo(selectBegin));

		// Transform
		transform.accept(context, found);
		assertThat(context.document.top.get().size(), equalTo(1));
		assertTreeEqual(context.document.top.get().get(0), end);
		assertThat(context.selection.getPath(), equalTo(selectEnd));

		// Undo
		context.history.undo(context);
		assertThat(context.document.top.get().size(), equalTo(1));
		assertTreeEqual(context.document.top.get().get(0), begin);
		assertThat(context.selection.getPath(), equalTo(selectBegin));

		// Redo
		context.history.redo(context);
		assertThat(context.document.top.get().size(), equalTo(1));
		assertTreeEqual(context.document.top.get().get(0), end);
		assertThat(context.selection.getPath(), equalTo(selectEnd));
	}

	@Test
	public void removeRootOnly() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.infinity).build(),
				new Path("0"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray(context.document.top, 0, 1, ImmutableList.of())
				),
				MiscSyntax.syntax.gap.create(),
				new Path("0")
		);
	}

	@Test
	public void removeArrayOnly() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0"),
				(context, selected) -> {
					context.history.apply(context,
							new ChangeArray((ValueArray) selected.parent.value(), 0, 1, ImmutableList.of())
					);
				},
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value").build(),
				new Path("0")
		);
	}

	@Test
	public void removeArraySelectBefore() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
						new Builders.TreeBuilder(MiscSyntax.infinity).build(),
						new Builders.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "0"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 1, 1, ImmutableList.of())
				),
				new Builders.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0")
		);
	}

	@Test
	public void removeArraySelectFollowing() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
						new Builders.TreeBuilder(MiscSyntax.infinity).build(),
						new Builders.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 0, 1, ImmutableList.of())
				),
				new Builders.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0")
		);
	}

	@Test
	public void removeArraySelectWithin() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
						new Builders.TreeBuilder(MiscSyntax.infinity).build(),
						new Builders.TreeBuilder(MiscSyntax.infinity).build(),
						new Builders.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 1, 1, ImmutableList.of())
				),
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
						new Builders.TreeBuilder(MiscSyntax.infinity).build(),
						new Builders.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1")
		);
	}

	@Test
	public void removeArraySelectWithinNoneAfter() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
						new Builders.TreeBuilder(MiscSyntax.infinity).build(),
						new Builders.TreeBuilder(MiscSyntax.infinity).build()
				).build(),
				new Path("0", "1"),
				(context, selected) -> context.history.apply(context,
						new ChangeArray((ValueArray) selected.parent.value(), 1, 1, ImmutableList.of())
				),
				new Builders.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "0")
		);
	}

	@Test
	public void removeArraySelectDeep() {
		innerTestTransform(MiscSyntax.syntax, new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
				new Builders.TreeBuilder(MiscSyntax.infinity).build(),
				new Builders.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Builders.TreeBuilder(MiscSyntax.infinity).build()
		).build(), new Path("0", "1", "0"), (context, selected) -> {
			((Value) context.locateLong(new Path("0", "1"))).parent.node().parent.delete(context);
		}, new Builders.TreeBuilder(MiscSyntax.array).addArray("value",
				new Builders.TreeBuilder(MiscSyntax.infinity).build(),
				new Builders.TreeBuilder(MiscSyntax.infinity).build()
		).build(), new Path("0", "1"));
	}

	@Test
	public void removeNode() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build(),
				new Path("0", "value"),
				(context, selected) -> {
					context.history.apply(context,
							new ChangeNodeSet((ValueNode) selected.parent.value(), MiscSyntax.syntax.gap.create())
					);
				},
				new Builders.TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				new Path("0", "value")
		);
	}

	@Test
	public void removeNodeSelectDeep() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.snooze).add("value",
						new Builders.TreeBuilder(MiscSyntax.array)
								.addArray("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
								.build()
				).build(),
				new Path("0", "value", "0"),
				(context, selected) -> {
					((ValueArray) context.locateLong(new Path("0", "value"))).parent.node().parent.delete(context);
				},
				new Builders.TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				new Path("0", "value")
		);
	}
}
