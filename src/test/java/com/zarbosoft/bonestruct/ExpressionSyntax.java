package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;

import static com.zarbosoft.bonestruct.Builders.buildBackDataNode;

public class ExpressionSyntax {
	final static FreeNodeType infinity;
	final static FreeNodeType plus;
	final static FreeNodeType minus;
	final static FreeNodeType multiply;
	final static FreeNodeType divide;
	final static FreeNodeType subscript;
	final static FreeNodeType inclusiveRange;
	final static Syntax syntax;

	static {
		infinity = new Builders.TypeBuilder("infinity")
				.back(Builders.buildBackPrimitive("infinity"))
				.front(new Builders.FrontMarkBuilder("infinity").build())
				.autoComplete(99)
				.build();
		plus = new Builders.TypeBuilder("plus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+")
				.frontDataNode("second")
				.precedence(10)
				.associateAfter()
				.autoComplete(99)
				.build();
		minus = new Builders.TypeBuilder("minus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("-")
				.frontDataNode("second")
				.precedence(10)
				.associateBefore()
				.autoComplete(99)
				.build();
		multiply = new Builders.TypeBuilder("multiply")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("*")
				.frontDataNode("second")
				.precedence(20)
				.associateAfter()
				.autoComplete(99)
				.build();
		divide = new Builders.TypeBuilder("divide")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("/")
				.frontDataNode("second")
				.precedence(20)
				.associateAfter()
				.autoComplete(99)
				.build();
		subscript = new Builders.TypeBuilder("subscript")
				.middleNode("first", "name")
				.middleNode("second", "name")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("_")
				.frontDataNode("second")
				.precedence(0)
				.autoComplete(99)
				.build();
		inclusiveRange = new Builders.TypeBuilder("inclusiveRange")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontMark("[")
				.frontDataNode("first")
				.frontMark(", ")
				.frontDataNode("second")
				.frontMark("]")
				.precedence(50)
				.autoComplete(99)
				.build();
		syntax = new Builders.SyntaxBuilder("any")
				.type(infinity)
				.type(plus)
				.type(minus)
				.type(multiply)
				.type(divide)
				.type(subscript)
				.type(inclusiveRange)
				.group("name", new Builders.GroupBuilder().type(infinity).type(subscript).build())
				.group(
						"any",
						new Builders.GroupBuilder()
								.type(plus)
								.type(minus)
								.type(multiply)
								.type(divide)
								.group("name")
								.type(inclusiveRange)
								.build()
				)
				.build();
	}
}
