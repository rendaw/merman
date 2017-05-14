package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;

public class ExpressionSyntax {
	final public static FreeAtomType infinity;
	final public static FreeAtomType factorial;
	final public static FreeAtomType plus;
	final public static FreeAtomType minus;
	final public static FreeAtomType multiply;
	final public static FreeAtomType divide;
	final public static FreeAtomType subscript;
	final public static FreeAtomType inclusiveRange;
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
