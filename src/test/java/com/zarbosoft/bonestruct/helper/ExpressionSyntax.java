package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;

public class ExpressionSyntax {
	final public static FreeNodeType infinity;
	final public static FreeNodeType factorial;
	final public static FreeNodeType plus;
	final public static FreeNodeType minus;
	final public static FreeNodeType multiply;
	final public static FreeNodeType divide;
	final public static FreeNodeType subscript;
	final public static FreeNodeType inclusiveRange;
	final public static Syntax syntax;

	static {
		infinity = new Helper.TypeBuilder("infinity")
				.back(Helper.buildBackPrimitive("infinity"))
				.front(new Helper.FrontMarkBuilder("infinity").build())
				.autoComplete(99)
				.build();
		factorial = new Helper.TypeBuilder("factorial")
				.middleNode("value", "any")
				.back(new Helper.BackRecordBuilder().add("value", Helper.buildBackDataNode("value")).build())
				.frontDataNode("value")
				.frontMark("!")
				.autoComplete(99)
				.build();
		plus = new Helper.TypeBuilder("plus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
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
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
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
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
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
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
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
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
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
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
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
