package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;

import static com.zarbosoft.bonestruct.Helper.*;

public class MiscSyntax {
	final static FreeNodeType infinity;
	final static FreeNodeType one;
	final static FreeNodeType two;
	final static FreeNodeType three;
	final static FreeNodeType four;
	final static FreeNodeType five;
	final static FreeNodeType multiback;
	final static FreeNodeType quoted;
	final static FreeNodeType binaryBang;
	final static FreeNodeType plusEqual;
	final static FreeNodeType plus;
	final static FreeNodeType waddle;
	final static FreeNodeType snooze;
	final static FreeNodeType multiplier;
	final static FreeNodeType array;
	final static FreeNodeType record;
	final static FreeNodeType recordElement;
	final static FreeNodeType pair;
	final static FreeNodeType ratio;
	final static Syntax syntax;

	static {
		infinity = new Helper.TypeBuilder("infinity")
				.back(Helper.buildBackPrimitive("infinity"))
				.front(new Helper.FrontMarkBuilder("infinity").build())
				.autoComplete(99)
				.build();
		one = new Helper.TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new Helper.FrontMarkBuilder("one").build())
				.build();
		two = new Helper.TypeBuilder("two")
				.back(Helper.buildBackPrimitive("two"))
				.front(new Helper.FrontMarkBuilder("two").build())
				.build();
		three = new Helper.TypeBuilder("three")
				.back(Helper.buildBackPrimitive("three"))
				.front(new Helper.FrontMarkBuilder("three").build())
				.build();
		four = new Helper.TypeBuilder("four")
				.back(Helper.buildBackPrimitive("four"))
				.front(new Helper.FrontMarkBuilder("four").build())
				.build();
		five = new Helper.TypeBuilder("five")
				.back(Helper.buildBackPrimitive("five"))
				.front(new Helper.FrontMarkBuilder("five").build())
				.build();
		multiback = new Helper.TypeBuilder("multiback")
				.back(buildBackDataPrimitive("a"))
				.back(buildBackDataPrimitive("b"))
				.middlePrimitive("a")
				.middlePrimitive("b")
				.frontDataPrimitive("a")
				.frontMark("^")
				.frontDataPrimitive("b")
				.build();
		quoted = new Helper.TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(buildBackDataPrimitive("value"))
				.front(new Helper.FrontMarkBuilder("\"").build())
				.frontDataPrimitive("value")
				.front(new Helper.FrontMarkBuilder("\"").build())
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
				.build();
		plusEqual = new Helper.TypeBuilder("plusequal")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+=")
				.frontDataNode("second")
				.build();
		binaryBang = new Helper.TypeBuilder("bang")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.autoComplete(99)
				.build();
		waddle = new Helper.TypeBuilder("waddle")
				.middleNode("first", "any")
				.back(new Helper.BackRecordBuilder().add("first", buildBackDataNode("first")).build())
				.frontDataNode("first")
				.frontMark("?")
				.autoComplete(99)
				.build();
		snooze = new Helper.TypeBuilder("snooze")
				.middleNode("value", "any")
				.back(new Helper.BackRecordBuilder().add("value", buildBackDataNode("value")).build())
				.frontMark("#")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		multiplier = new Helper.TypeBuilder("multiplier")
				.middlePrimitive("text")
				.middleNode("value", "any")
				.back(new Helper.BackRecordBuilder()
						.add("value", buildBackDataNode("value"))
						.add("text", buildBackDataPrimitive("text"))
						.build())
				.frontMark("x")
				.frontDataPrimitive("text")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		array = new Helper.TypeBuilder("array")
				.middleArray("value", "any")
				.back(buildBackDataArray("value"))
				.frontMark("<")
				.front(new FrontDataArrayBuilder("value").addSeparator(new FrontMarkBuilder(", ").build()).build())
				.frontMark(">")
				.autoComplete(99)
				.build();
		record = new Helper.TypeBuilder("record")
				.middleRecord("value", "record_element")
				.back(buildBackDataRecord("value"))
				.frontMark("{")
				.frontDataArray("value")
				.frontMark("}")
				.autoComplete(99)
				.build();
		recordElement = new Helper.TypeBuilder("record_element")
				.middleRecordKey("key")
				.middleNode("value", "any")
				.back(buildBackDataKey("key"))
				.back(buildBackDataNode("value"))
				.frontDataPrimitive("key")
				.frontMark(": ")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		pair = new Helper.TypeBuilder("pair")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackArrayBuilder()
						.add(buildBackDataNode("first"))
						.add(buildBackDataNode("second"))
						.build())
				.frontMark("<")
				.frontDataNode("first")
				.frontMark(", ")
				.frontDataNode("second")
				.frontMark(">")
				.autoComplete(99)
				.build();
		ratio = new Helper.TypeBuilder("ratio")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.back(new BackRecordBuilder()
						.add("first", buildBackDataPrimitive("first"))
						.add("second", buildBackDataPrimitive("second"))
						.build())
				.frontMark("<")
				.frontDataPrimitive("first")
				.frontMark(":")
				.frontDataPrimitive("second")
				.frontMark(">")
				.build();
		syntax = new Helper.SyntaxBuilder("any")
				.type(infinity)
				.type(one)
				.type(two)
				.type(three)
				.type(four)
				.type(five)
				.type(multiback)
				.type(quoted)
				.type(plus)
				.type(plusEqual)
				.type(binaryBang)
				.type(waddle)
				.type(snooze)
				.type(multiplier)
				.type(array)
				.type(record)
				.type(recordElement)
				.type(pair)
				.type(ratio)
				.group(
						"test_group_1",
						new Helper.GroupBuilder()
								.type(infinity)
								.type(MiscSyntax.one)
								.type(MiscSyntax.multiback)
								.group("test_group_2")
								.build()
				)
				.group("test_group_2", new Helper.GroupBuilder().type(quoted).build())
				.group(
						"any",
						new Helper.GroupBuilder()
								.type(infinity)
								.type(one)
								.type(two)
								.type(three)
								.type(four)
								.type(five)
								.type(quoted)
								.type(plus)
								.type(plusEqual)
								.type(binaryBang)
								.type(waddle)
								.type(snooze)
								.type(multiplier)
								.type(array)
								.type(record)
								.type(pair)
								.type(ratio)
								.build()
				)
				.group("arrayChildren", new Helper.GroupBuilder().type(one).type(MiscSyntax.multiback).build())
				.build();
	}
}
