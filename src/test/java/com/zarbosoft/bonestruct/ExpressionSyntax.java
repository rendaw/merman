package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;

import static com.zarbosoft.bonestruct.Helper.buildBackDataNode;

public class ExpressionSyntax {
	final static FreeNodeType infinity;
	final static FreeNodeType factorial;
	final static FreeNodeType plus;
	final static FreeNodeType minus;
	final static FreeNodeType multiply;
	final static FreeNodeType divide;
	final static FreeNodeType subscript;
	final static FreeNodeType inclusiveRange;
	final static Syntax syntax;

	static {
		infinity = new Helper.TypeBuilder("infinity")
				.back(Helper.buildBackPrimitive("infinity"))
				.front(new Helper.FrontMarkBuilder("infinity").build())
				.autoComplete(99)
				.build();
		factorial = new Helper.TypeBuilder("factorial")
				.middleNode("value", "any")
				.back(new Helper.BackRecordBuilder().add("value", buildBackDataNode("value")).build())
				.frontDataNode("value")
				.frontMark("!")
				.autoComplete(99)
				.build();
		plus = new Helper.TypeBuilder("plus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
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
		minus = new Helper.TypeBuilder("minus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
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
		multiply = new Helper.TypeBuilder("multiply")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
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
		divide = new Helper.TypeBuilder("divide")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
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
		subscript = new Helper.TypeBuilder("subscript")
				.middleNode("first", "name")
				.middleNode("second", "name")
				.back(new Helper.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("_")
				.frontDataNode("second")
				.precedence(0)
				.autoComplete(99)
				.build();
		inclusiveRange = new Helper.TypeBuilder("inclusiveRange")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
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
		syntax = new Helper.SyntaxBuilder("any")
				.type(infinity)
				.type(factorial)
				.type(plus)
				.type(minus)
				.type(multiply)
				.type(divide)
				.type(subscript)
				.type(inclusiveRange)
				.group("name", new Helper.GroupBuilder().type(infinity).type(subscript).build())
				.group(
						"any",
						new Helper.GroupBuilder()
								.type(factorial)
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
