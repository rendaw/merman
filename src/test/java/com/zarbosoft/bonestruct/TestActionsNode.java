package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import org.junit.Test;

import static com.zarbosoft.bonestruct.Builders.assertTreeEqual;
import static com.zarbosoft.bonestruct.Builders.buildDoc;
import static com.zarbosoft.rendaw.common.Common.iterable;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsNode {
	private static void act(final Context context, final String name) {
		for (final Action action : iterable(context.actions.entrySet().stream().flatMap(e -> e.getValue().stream()))) {
			if (action.getName().equals(name)) {
				action.run(context);
				return;
			}
		}
		throw new AssertionError(String.format("No action named [%s]", name));
	}

	@Test
	public void testEnter() {
		final Context context = buildDoc(MiscSyntax.syntax, new Builders.TreeBuilder(MiscSyntax.snooze).add(
				"value",
				new Builders.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build()
		).build());
		((Node) context.locateLong(new Path("0", "value"))).getVisual().parent().getTarget().select(context);
		act(context, "enter");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "value", "value")));
	}

	@Test
	public void testExit() {
		final Context context = buildDoc(MiscSyntax.syntax, new Builders.TreeBuilder(MiscSyntax.snooze).add(
				"value",
				new Builders.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build()
		).build());
		((Node) context.locateLong(new Path("0", "value"))).getVisual().parent().getTarget().select(context);
		act(context, "exit");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0")));
	}

	@Test
	public void testDelete() {
		final Context context = buildDoc(MiscSyntax.syntax, new Builders.TreeBuilder(MiscSyntax.snooze).add(
				"value",
				new Builders.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Builders.TreeBuilder(MiscSyntax.infinity).build())
						.build()
		).build());
		((Node) context.locateLong(new Path("0", "value"))).getVisual().parent().getTarget().select(context);
		act(context, "delete");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				context.document.top
		);
	}

	@Test
	public void testCopyPaste() {
		final Context context = buildDoc(
				ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity).build())
						.add("second", ExpressionSyntax.syntax.gap.create())
						.build()
		);
		((ValueNode) context.locateShort(new Path("0", "first"))).getVisual().select(context);
		act(context, "copy");
		((ValueNode) context.locateShort(new Path("0", "second"))).getVisual().select(context);
		act(context, "paste");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity).build())
						.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity).build())
						.build(),
				context.document.top
		);
	}

	@Test
	public void testCutPaste() {
		final Context context = buildDoc(
				ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new Builders.TreeBuilder(ExpressionSyntax.infinity).build())
						.build()
		);
		((ValueNode) context.locateShort(new Path("0", "value"))).getVisual().select(context);
		act(context, "cut");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(ExpressionSyntax.factorial)
						.add("value", ExpressionSyntax.syntax.gap.create())
						.build(),
				context.document.top
		);
		act(context, "paste");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new Builders.TreeBuilder(ExpressionSyntax.infinity).build())
						.build(),
				context.document.top
		);
	}
}
