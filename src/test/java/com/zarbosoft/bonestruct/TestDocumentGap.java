package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.model.Document;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.zarbosoft.bonestruct.Builders.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {

	private Context blank() {
		final Document doc = MiscSyntax.syntax.create();
		final Context context = new Context(MiscSyntax.syntax, doc, null, null, null, new History());
		final Node gap = MiscSyntax.syntax.gap.create();
		context.history.apply(context, new DataArrayBase.ChangeAdd(doc.top, 0, ImmutableList.of(gap)));
		final VisualNodePart visual =
				MiscSyntax.syntax.rootFront.createVisual(context, ImmutableMap.of("value", doc.top), ImmutableSet.of());
		gap.getVisual().select(context);
		return context;
	}

	@Test
	public void syntaxLeafNodes() {
		final Context context = blank();
		assertThat(context.syntax.getLeafTypes("test-group-1").collect(Collectors.toSet()),
				equalTo(ImmutableSet.of(MiscSyntax.infinity, MiscSyntax.one, MiscSyntax.multiback, MiscSyntax.quoted))
		);
	}

	// ========================================================================
	// Decision making and replacement

	@Test
	public void undecided() {
		final Context context = blank();
		context.selection.receiveText(context, "o");
		assertTreeEqual(context, new Builders.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "o"), context.document.top);
	}

	@Test
	public void undecidedFull() {
		final Context context = blank();
		context.selection.receiveText(context, "o");
		context.selection.receiveText(context, "n");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "one"),
				context.document.top
		);
	}

	@Test
	public void immediate() {
		final Context context = blank();
		context.selection.receiveText(context, "i");
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.syntax.suffixGap)
						.addArray("value", ImmutableList.of(new Builders.TreeBuilder(MiscSyntax.infinity).build()))
						.add("gap", ""),
				context.document.top
		);
	}

	@Test
	public void binaryImmediate() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "!");
		dump(context.document.top);
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.binaryBang)
						.add("first", new Builders.TreeBuilder(MiscSyntax.one))
						.add("second", new Builders.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "")),
				context.document.top
		);
	}

	@Test
	public void binaryUndecided() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "+");
		dump(context.document.top);
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.syntax.suffixGap)
						.addArray("value", ImmutableList.of(new Builders.TreeBuilder(MiscSyntax.one).build()))
						.add("gap", "+"),
				context.document.top
		);
	}

	// ========================================================================
	// Unit gap
	@Test
	public void unitContinueInside() {
		final Context context = blank();
		context.selection.receiveText(context, "\"");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context, new Builders.TreeBuilder(MiscSyntax.quoted).add("value", "e"), context.document.top);
	}

	// ========================================================================
	// Suffix
	@Test
	public void suffixContinueInside() {
		final Context context = buildDoc(MiscSyntax.syntax,
				MiscSyntax.syntax.suffixGap.create(true, new Builders.TreeBuilder(MiscSyntax.one).build())
		);
		context.document.top.get().get(0).getVisual().select(context);
		context.selection.receiveText(context, "?");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context, new Builders.TreeBuilder(MiscSyntax.syntax.suffixGap).addArray("value",
				ImmutableList.of(new Builders.TreeBuilder(MiscSyntax.waddle)
						.add("first", new Builders.TreeBuilder(MiscSyntax.one))
						.build())
		).add("gap", "e"), context.document.top);
	}

	// ========================================================================
	// Prefix
	@Test
	public void prefixContinue() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.syntax.prefixGap)
						.add("gap", "")
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.one).build())
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "gap"))).getVisual().select(context);
		context.selection.receiveText(context, "x");
		context.selection.receiveText(context, "13");
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.multiplier)
						.add("value", new Builders.TreeBuilder(MiscSyntax.one))
						.add("text", "13"),
				context.document.top
		);
	}

	@Test
	public void prefixContinueWrap() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.syntax.prefixGap)
						.add("gap", "")
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.one).build())
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "gap"))).getVisual().select(context);
		context.selection.receiveText(context, "#");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context, new Builders.TreeBuilder(MiscSyntax.snooze).add("value",
				new Builders.TreeBuilder(context.syntax.prefixGap)
						.add("gap", "e")
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.one).build())
		), context.document.top);
	}

	// ========================================================================
	// Suffix raising

	@Test
	public void testRaisePrecedenceLower() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
		context.selection.receiveText(context, "*");
		assertTreeEqual(context,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Builders.TreeBuilder(ExpressionSyntax.multiply)
										.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", context.syntax.gap.create())
						),
				context.document.top
		);
	}

	@Test
	public void testRaisePrecedenceEqualAfter() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
		dump(context.document.top);
		context.selection.receiveText(context, "+");
		dump(context.document.top);
		assertTreeEqual(context,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Builders.TreeBuilder(ExpressionSyntax.plus)
										.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", context.syntax.gap.create())
						),
				context.document.top
		);
	}

	@Test
	public void testRaisePrecedenceEqualBefore() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.minus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
		context.selection.receiveText(context, "-");
		assertTreeEqual(context, new Builders.TreeBuilder(ExpressionSyntax.minus).add("first",
				new Builders.TreeBuilder(ExpressionSyntax.minus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity))
		).add("second", context.syntax.gap.create()), context.document.top);
	}

	@Test
	public void testRaisePrecedenceGreater() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.multiply)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
		context.selection.receiveText(context, "+");
		assertTreeEqual(context, new Builders.TreeBuilder(ExpressionSyntax.plus).add("first",
				new Builders.TreeBuilder(ExpressionSyntax.multiply)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity))
		).add("second", context.syntax.gap.create()), context.document.top);
	}

	@Test
	public void testRaiseSkipDissimilar() {
		final Context context = buildDoc(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.subscript)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build()
		);
		((DataPrimitive.Value) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
		context.selection.receiveText(context, "+");
		assertTreeEqual(context, new Builders.TreeBuilder(ExpressionSyntax.plus).add("first",
				new Builders.TreeBuilder(ExpressionSyntax.subscript)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity))
		).add("second", context.syntax.gap.create()), context.document.top);
	}

	// ========================================================================
	// Deselection removal
}
