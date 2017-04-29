package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import org.junit.Test;

import static com.zarbosoft.bonestruct.Helper.assertTreeEqual;
import static com.zarbosoft.bonestruct.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsNode {

	@Test
	public void testEnter() {
		final Context context = buildDoc(MiscSyntax.syntax, new Helper.TreeBuilder(MiscSyntax.snooze).add(
				"value",
				new Helper.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build()
		).build());
		((Node) context.locateLong(new Path("0", "value"))).getVisual().parent().getTarget().select(context);
		Helper.act(context, "enter");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "value", "value")));
	}

	@Test
	public void testExit() {
		final Context context = buildDoc(MiscSyntax.syntax, new Helper.TreeBuilder(MiscSyntax.snooze).add(
				"value",
				new Helper.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build()
		).build());
		((Node) context.locateLong(new Path("0", "value"))).getVisual().parent().getTarget().select(context);
		Helper.act(context, "exit");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0")));
	}

	@Test
	public void testDelete() {
		final Context context = buildDoc(MiscSyntax.syntax, new Helper.TreeBuilder(MiscSyntax.snooze).add(
				"value",
				new Helper.TreeBuilder(MiscSyntax.snooze)
						.add("value", new Helper.TreeBuilder(MiscSyntax.infinity).build())
						.build()
		).build());
		((Node) context.locateLong(new Path("0", "value"))).getVisual().parent().getTarget().select(context);
		Helper.act(context, "delete");
		assertTreeEqual(
				context,
				new Helper.TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				context.document.top
		);
	}

	@Test
	public void testCopyPaste() {
		final Context context = buildDoc(
				ExpressionSyntax.syntax,
				new Helper.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity).build())
						.add("second", ExpressionSyntax.syntax.gap.create())
						.build()
		);
		((ValueNode) context.locateShort(new Path("0", "first"))).getVisual().select(context);
		Helper.act(context, "copy");
		((ValueNode) context.locateShort(new Path("0", "second"))).getVisual().select(context);
		Helper.act(context, "paste");
		assertTreeEqual(
				context,
				new Helper.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Helper.TreeBuilder(ExpressionSyntax.infinity).build())
						.add("second", new Helper.TreeBuilder(ExpressionSyntax.infinity).build())
						.build(),
				context.document.top
		);
	}

	@Test
	public void testCutPaste() {
		final Context context = buildDoc(
				ExpressionSyntax.syntax,
				new Helper.TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new Helper.TreeBuilder(ExpressionSyntax.infinity).build())
						.build()
		);
		((ValueNode) context.locateShort(new Path("0", "value"))).getVisual().select(context);
		Helper.act(context, "cut");
		assertTreeEqual(
				context,
				new Helper.TreeBuilder(ExpressionSyntax.factorial)
						.add("value", ExpressionSyntax.syntax.gap.create())
						.build(),
				context.document.top
		);
		Helper.act(context, "paste");
		assertTreeEqual(
				context,
				new Helper.TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new Helper.TreeBuilder(ExpressionSyntax.infinity).build())
						.build(),
				context.document.top
		);
	}
}
