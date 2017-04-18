package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.history.History;
import com.zarbosoft.bonestruct.history.changes.ChangeArrayAdd;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zarbosoft.bonestruct.Builders.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {

	private Context blank() {
		final Document doc = MiscSyntax.syntax.create();
		final Context context = new Context(MiscSyntax.syntax, doc, null, null, new History());
		final Node gap = MiscSyntax.syntax.gap.create();
		context.history.apply(context, new ChangeArrayAdd(doc.top, 0, ImmutableList.of(gap)));
		final VisualPart visual =
				MiscSyntax.syntax.rootFront.createVisual(context, ImmutableMap.of("value", doc.top), ImmutableSet.of());
		gap.getVisual().select(context);
		return context;
	}

	private void innerTestTransform(
			final Syntax syntax, final Node start, final Consumer<Context> transform, final Node end
	) {
		final Context context = buildDoc(syntax, start);
		transform.accept(context);
		assertTreeEqual(context, end, context.document.top);
		context.history.undo(context);
		assertTreeEqual(context, start, context.document.top);
		context.history.redo(context);
		assertTreeEqual(context, end, context.document.top);
	}

	@Test
	public void syntaxLeafNodes() {
		final Context context = blank();
		assertThat(context.syntax.getLeafTypes("test_group_1").collect(Collectors.toSet()),
				equalTo(ImmutableSet.of(MiscSyntax.infinity, MiscSyntax.one, MiscSyntax.multiback, MiscSyntax.quoted))
		);
	}

	// ========================================================================
	// Decision making and replacement

	@Test
	public void undecided() {
		final Context context = blank();
		context.selection.receiveText(context, "o");
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "o").build(),
				context.document.top
		);
	}

	@Test
	public void undecidedFull() {
		final Context context = blank();
		context.selection.receiveText(context, "o");
		context.selection.receiveText(context, "n");
		context.selection.receiveText(context, "e");
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "one").build(),
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
						.add("gap", "")
						.build(),
				context.document.top
		);
	}

	@Test
	public void binaryImmediate() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "!");
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.binaryBang)
						.add("first", new Builders.TreeBuilder(MiscSyntax.one))
						.add("second", new Builders.TreeBuilder(MiscSyntax.syntax.gap).add("gap", ""))
						.build(),
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
						.add("gap", "+")
						.build(),
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
		assertTreeEqual(context,
				new Builders.TreeBuilder(MiscSyntax.quoted).add("value", "e").build(),
				context.document.top
		);
	}

	// ========================================================================
	// Suffix

	@Test
	public void suffixContinueInside() {
		innerTestTransform(MiscSyntax.syntax,
				MiscSyntax.syntax.suffixGap.create(true, new Builders.TreeBuilder(MiscSyntax.one).build()),
				context -> {
					context.document.top.get().get(0).getVisual().select(context);
					context.selection.receiveText(context, "?");
					context.selection.receiveText(context, "e");
				},
				new Builders.TreeBuilder(MiscSyntax.syntax.suffixGap).addArray("value",
						ImmutableList.of(new Builders.TreeBuilder(MiscSyntax.waddle)
								.add("first", new Builders.TreeBuilder(MiscSyntax.one))
								.build())
				).add("gap", "e").build()
		);
	}

	// ========================================================================
	// Prefix
	@Test
	public void prefixContinue() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.syntax.prefixGap)
						.add("gap", "")
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.one).build())
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "x");
					context.selection.receiveText(context, "13");
				},
				new Builders.TreeBuilder(MiscSyntax.multiplier)
						.add("value", new Builders.TreeBuilder(MiscSyntax.one))
						.add("text", "13")
						.build()
		);
	}

	@Test
	public void prefixContinueWrap() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.syntax.prefixGap)
						.add("gap", "")
						.addArray("value", new Builders.TreeBuilder(MiscSyntax.one).build())
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "#");
					context.selection.receiveText(context, "e");
				},
				new Builders.TreeBuilder(MiscSyntax.snooze).add("value",
						new Builders.TreeBuilder(MiscSyntax.syntax.prefixGap)
								.add("gap", "e")
								.addArray("value", new Builders.TreeBuilder(MiscSyntax.one).build())
				).build()
		);
	}

	// ========================================================================
	// Suffix raising

	@Test
	public void testRaisePrecedenceLower() {
		innerTestTransform(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "*");
				},
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Builders.TreeBuilder(ExpressionSyntax.multiply)
										.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", ExpressionSyntax.syntax.gap.create())
						)
						.build()
		);
	}

	@Test
	public void testRaisePrecedenceEqualAfter() {
		innerTestTransform(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "+");
				},
				new Builders.TreeBuilder(ExpressionSyntax.plus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Builders.TreeBuilder(ExpressionSyntax.plus)
										.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", ExpressionSyntax.syntax.gap.create())
						)
						.build()
		);
	}

	@Test
	public void testRaisePrecedenceEqualBefore() {
		innerTestTransform(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.minus)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "-");
				},
				new Builders.TreeBuilder(ExpressionSyntax.minus).add("first",
						new Builders.TreeBuilder(ExpressionSyntax.minus)
								.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
								.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity))
				).add("second", ExpressionSyntax.syntax.gap.create()).build()
		);
	}

	@Test
	public void testRaisePrecedenceGreater() {
		innerTestTransform(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.multiply)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "+");
				},
				new Builders.TreeBuilder(ExpressionSyntax.plus).add("first",
						new Builders.TreeBuilder(ExpressionSyntax.multiply)
								.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
								.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity))
				).add("second", ExpressionSyntax.syntax.gap.create()).build()
		);
	}

	@Test
	public void testRaiseSkipDissimilar() {
		innerTestTransform(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.subscript)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "+");
				},
				new Builders.TreeBuilder(ExpressionSyntax.plus).add("first",
						new Builders.TreeBuilder(ExpressionSyntax.subscript)
								.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
								.add("second", new Builders.TreeBuilder(ExpressionSyntax.infinity))
				).add("second", ExpressionSyntax.syntax.gap.create()).build()
		);
	}

	@Test
	public void testRaiseBounded() {
		innerTestTransform(ExpressionSyntax.syntax,
				new Builders.TreeBuilder(ExpressionSyntax.inclusiveRange)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								ExpressionSyntax.syntax.suffixGap.create(true,
										new Builders.TreeBuilder(ExpressionSyntax.infinity).build()
								)
						)
						.build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "second", "gap"))).getVisual().select(context);
					context.selection.receiveText(context, "+");
				},
				new Builders.TreeBuilder(ExpressionSyntax.inclusiveRange)
						.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
						.add("second",
								new Builders.TreeBuilder(ExpressionSyntax.plus)
										.add("first", new Builders.TreeBuilder(ExpressionSyntax.infinity))
										.add("second", ExpressionSyntax.syntax.gap.create())
						)
						.build()
		);
	}

	// ========================================================================
	// Deselection removal

	@Test
	public void testDropArrayElement() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value", MiscSyntax.syntax.gap.create()).build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "0"))).getVisual().select(context);
					((ValueArray) context.locate(new Path("0"))).getVisual().select(context);
				},
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value").build()
		);
	}

	@Test
	public void testDontDropNodeGap() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value", MiscSyntax.syntax.gap.create()).build(),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "0"))).getVisual().select(context);
					context.selection.receiveText(context, "urt");
					((ValueArray) context.locate(new Path("0"))).getVisual().select(context);
				},
				new Builders.TreeBuilder(MiscSyntax.array)
						.addArray("value", new TreeBuilder(MiscSyntax.syntax.gap).add("gap", "urt").build())
						.build()
		);
	}

	@Test
	public void testDropSuffixValue() {
		innerTestTransform(MiscSyntax.syntax,
				MiscSyntax.syntax.suffixGap.create(true, new TreeBuilder(MiscSyntax.infinity).build()),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "gap"))).getVisual().select(context);
					((Node) context.locate(new Path("0"))).getVisual().select(context);
				},
				new Builders.TreeBuilder(MiscSyntax.infinity).build()
		);
	}

	@Test
	public void testDropPrefixValue() {
		innerTestTransform(MiscSyntax.syntax,
				MiscSyntax.syntax.prefixGap.create(new TreeBuilder(MiscSyntax.infinity).build()),
				context -> {
					((ValuePrimitive) context.locate(new Path("0", "gap"))).getVisual().select(context);
					((Node) context.locate(new Path("0"))).getVisual().select(context);
				},
				new Builders.TreeBuilder(MiscSyntax.infinity).build()
		);
	}

	// ========================================================================
	// Array gap creation

	@Test
	public void testCreateArrayGap() {
		innerTestTransform(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value").build(),
				context -> {
					((ValueArray) context.locate(new Path("0"))).getVisual().select(context);
				},
				new Builders.TreeBuilder(MiscSyntax.array).addArray("value", MiscSyntax.syntax.gap.create()).build()
		);
	}
}
