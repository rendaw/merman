package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.model.Document;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import org.junit.Test;

import static com.zarbosoft.bonestruct.Builders.assertTreeEqual;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {
	final static FreeNodeType infinity;
	final static FreeNodeType one;
	final static FreeNodeType two;
	final static FreeNodeType five;
	final static FreeNodeType binaryBang;
	final static FreeNodeType plusEqual;
	final static FreeNodeType plus;
	final static Syntax syntax;

	static {
		infinity = new Builders.TypeBuilder("infinity")
				.back(Builders.buildBackPrimitive("infinity"))
				.front(new Builders.FrontMarkBuilder("infinity").build())
				.immediate()
				.build();
		one = new Builders.TypeBuilder("one")
				.back(Builders.buildBackPrimitive("one"))
				.front(new Builders.FrontMarkBuilder("one").build())
				.build();
		two = new Builders.TypeBuilder("two")
				.back(Builders.buildBackPrimitive("two"))
				.front(new Builders.FrontMarkBuilder("two").build())
				.build();
		five = new Builders.TypeBuilder("five")
				.back(Builders.buildBackPrimitive("five"))
				.front(new Builders.FrontMarkBuilder("five").build())
				.build();
		plus = new Builders.TypeBuilder("plus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", Builders.buildBackDataNode("first"))
						.add("second", Builders.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+")
				.frontDataNode("second")
				.build();
		plusEqual = new Builders.TypeBuilder("plusequal")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", Builders.buildBackDataNode("first"))
						.add("second", Builders.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+=")
				.frontDataNode("second")
				.build();
		binaryBang = new Builders.TypeBuilder("bang")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", Builders.buildBackDataNode("first"))
						.add("second", Builders.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.immediate()
				.build();
		syntax = new Builders.SyntaxBuilder("any")
				.type(infinity)
				.type(one)
				.type(two)
				.type(five)
				.type(plus)
				.type(plusEqual)
				.type(binaryBang)
				.group(
						"any",
						new Builders.GroupBuilder()
								.type(infinity)
								.type(one)
								.type(two)
								.type(five)
								.type(plus)
								.type(plusEqual)
								.type(binaryBang)
								.build()
				)
				.build();
	}

	private Context blank() {
		final Document doc = syntax.create();
		final Context context = new Context(syntax, doc, null, null, null, new History());
		final Node gap = syntax.gap.create();
		context.history.apply(context, new DataArrayBase.ChangeAdd(doc.top, 0, ImmutableList.of(gap)));
		final VisualNodePart visual =
				syntax.rootFront.createVisual(context, ImmutableMap.of("value", doc.top), ImmutableSet.of());
		gap.getVisual().select(context);
		return context;
	}

	@Test
	public void syntaxLeafNodes() {
		final Context context = blank();
		assertThat(
				context.syntax.getLeafTypes("any"),
				equalTo(ImmutableSet.of(infinity, one, two, five, plus, plusEqual, binaryBang))
		);
	}

	@Test
	public void undecided() {
		final Context context = blank();
		context.selection.receiveText(context, "i");
		assertTreeEqual(context, new Builders.TreeBuilder(syntax.gap).add("gap", "i"), context.document.top);
	}

	@Test
	public void nullary() {
		final Context context = blank();
		context.selection.receiveText(context, "infinity");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(syntax.suffixGap)
						.add("value", new Builders.TreeBuilder(infinity))
						.add("gap", ""),
				context.document.top
		);
	}

	@Test
	public void binaryImmediate() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "!");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(binaryBang)
						.add("first", new Builders.TreeBuilder(one))
						.add("second", new Builders.TreeBuilder(syntax.gap)),
				context.document.top
		);
	}

	@Test
	public void binary() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "+");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(syntax.suffixGap).add("value", new Builders.TreeBuilder(one)).add("gap", "+"),
				context.document.top
		);
	}

	@Test
	public void binaryContinuation() {
		final Context context = blank();
		context.selection.receiveText(context, "one");
		context.selection.receiveText(context, "+");
		context.selection.receiveText(context, "t");
		assertTreeEqual(
				context,
				new Builders.TreeBuilder(plus)
						.add("first", new Builders.TreeBuilder(one))
						.add("second", new Builders.TreeBuilder(syntax.gap).add("value", "t")),
				context.document.top
		);
	}

	@Test
	public void prefixPlacement() {

	}

	@Test
	public void suffixPlacement() {

	}

	@Test
	public void suffixBubbleNone() {

	}

	@Test
	public void suffixBubbleSimilar() {

	}

	@Test
	public void suffixBubbleDissimilarMiddle() {

	}

	@Test
	public void suffixBubbleAssociativePrior() {

	}

	@Test
	public void suffixBubbleAssociativeAfter() {

	}

}
