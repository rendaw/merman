package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.helper.*;
import org.junit.Test;

import static com.zarbosoft.bonestruct.helper.Helper.assertTreeEqual;
import static com.zarbosoft.bonestruct.helper.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestActionsNested {

	@Test
	public void testEnter() {
		final Context context = buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.snooze).add("value",
				new TreeBuilder(MiscSyntax.snooze).add("value", new TreeBuilder(MiscSyntax.infinity).build()).build()
		).build());
		((Atom) context.locateLong(new Path("0", "value"))).parent.selectUp(context);
		Helper.act(context, "enter");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "value", "value")));
	}

	@Test
	public void testExit() {
		final Context context = buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.snooze).add("value",
				new TreeBuilder(MiscSyntax.snooze).add("value", new TreeBuilder(MiscSyntax.infinity).build()).build()
		).build());
		((Atom) context.locateLong(new Path("0", "value"))).parent.selectUp(context);
		Helper.act(context, "exit");
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0")));
	}

	@Test
	public void testNext() {
		final Atom target = new TreeBuilder(MiscSyntax.one).build();
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.plus)
						.add("first", target)
						.add("second", new TreeBuilder(MiscSyntax.one).build())
						.build()
		)
				.run(context -> target.parent.selectUp(context))
				.act("next")
				.run(context -> assertThat(context.selection.getPath().toList(),
						equalTo(ImmutableList.of("0", "second"))
				));
	}

	@Test
	public void testPrevious() {
		final Atom target = new TreeBuilder(MiscSyntax.one).build();
		new GeneralTestWizard(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.plus)
						.add("first", new TreeBuilder(MiscSyntax.one).build())
						.add("second", target)
						.build()
		)
				.run(context -> target.parent.selectUp(context))
				.act("previous")
				.run(context -> assertThat(context.selection.getPath().toList(),
						equalTo(ImmutableList.of("0", "first"))
				));
	}

	@Test
	public void testDelete() {
		final Context context = buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.snooze).add("value",
				new TreeBuilder(MiscSyntax.snooze).add("value", new TreeBuilder(MiscSyntax.infinity).build()).build()
		).build());
		((Atom) context.locateLong(new Path("0", "value"))).parent.selectUp(context);
		Helper.act(context, "delete");
		assertTreeEqual(context,
				new TreeBuilder(MiscSyntax.snooze).add("value", MiscSyntax.syntax.gap.create()).build(),
				Helper.rootArray(context.document)
		);
	}

	@Test
	public void testCopyPaste() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new TreeBuilder(ExpressionSyntax.plus)
						.add("first", new TreeBuilder(ExpressionSyntax.infinity).build())
						.add("second", ExpressionSyntax.syntax.gap.create())
						.build()
		);
		((ValueAtom) context.locateShort(new Path("0", "first"))).visual.select(context);
		Helper.act(context, "copy");
		((ValueAtom) context.locateShort(new Path("0", "second"))).visual.select(context);
		Helper.act(context, "paste");
		assertTreeEqual(context,
				new TreeBuilder(ExpressionSyntax.plus)
						.add("first", new TreeBuilder(ExpressionSyntax.infinity).build())
						.add("second", new TreeBuilder(ExpressionSyntax.infinity).build())
						.build(),
				Helper.rootArray(context.document)
		);
	}

	@Test
	public void testCutPaste() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new TreeBuilder(ExpressionSyntax.infinity).build())
						.build()
		);
		((ValueAtom) context.locateShort(new Path("0", "value"))).visual.select(context);
		Helper.act(context, "cut");
		assertTreeEqual(context,
				new TreeBuilder(ExpressionSyntax.factorial).add("value", ExpressionSyntax.syntax.gap.create()).build(),
				Helper.rootArray(context.document)
		);
		Helper.act(context, "paste");
		assertTreeEqual(context,
				new TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new TreeBuilder(ExpressionSyntax.infinity).build())
						.build(),
				Helper.rootArray(context.document)
		);
	}

	@Test
	public void testPrefix() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new TreeBuilder(ExpressionSyntax.infinity).build())
						.build()
		);
		((ValueAtom) context.locateShort(new Path("0", "value"))).visual.select(context);
		Helper.act(context, "prefix");
		assertTreeEqual(context, new TreeBuilder(ExpressionSyntax.factorial)
				.add("value",
						ExpressionSyntax.syntax.prefixGap.create(new TreeBuilder(ExpressionSyntax.infinity).build())
				)
				.build(), Helper.rootArray(context.document));
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "value", "gap", "0")));
	}

	@Test
	public void testSuffix() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new TreeBuilder(ExpressionSyntax.factorial)
						.add("value", new TreeBuilder(ExpressionSyntax.infinity).build())
						.build()
		);
		((ValueAtom) context.locateShort(new Path("0", "value"))).visual.select(context);
		Helper.act(context, "suffix");
		assertTreeEqual(context,
				new TreeBuilder(ExpressionSyntax.factorial)
						.add("value",
								ExpressionSyntax.syntax.suffixGap.create(false,
										new TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				Helper.rootArray(context.document)
		);
		assertThat(context.selection.getPath().toList(), equalTo(ImmutableList.of("0", "value", "gap", "0")));
	}
}
