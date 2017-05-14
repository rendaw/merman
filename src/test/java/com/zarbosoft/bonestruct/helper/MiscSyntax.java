package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;

public class MiscSyntax {
	final public static FreeAtomType infinity;
	final public static FreeAtomType one;
	final public static FreeAtomType two;
	final public static FreeAtomType three;
	final public static FreeAtomType four;
	final public static FreeAtomType five;
	final public static FreeAtomType multiback;
	final public static FreeAtomType quoted;
	final public static FreeAtomType binaryBang;
	final public static FreeAtomType plusEqual;
	final public static FreeAtomType plus;
	final public static FreeAtomType waddle;
	final public static FreeAtomType snooze;
	final public static FreeAtomType multiplier;
	final public static FreeAtomType array;
	final public static FreeAtomType record;
	final public static FreeAtomType recordElement;
	final public static FreeAtomType pair;
	final public static FreeAtomType ratio;
	final public static Syntax syntax;

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
				.back(Helper.buildBackDataPrimitive("a"))
				.back(Helper.buildBackDataPrimitive("b"))
				.middlePrimitive("a")
				.middlePrimitive("b")
				.frontDataPrimitive("a")
				.frontMark("^")
				.frontDataPrimitive("b")
				.build();
		quoted = new Helper.TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.front(new Helper.FrontMarkBuilder("\"").build())
				.frontDataPrimitive("value")
				.front(new Helper.FrontMarkBuilder("\"").build())
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
				.build();
		plusEqual = new Helper.TypeBuilder("plusequal")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+=")
				.frontDataNode("second")
				.build();
		binaryBang = new Helper.TypeBuilder("bang")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackRecordBuilder()
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.autoComplete(99)
				.build();
		waddle = new Helper.TypeBuilder("waddle")
				.middleNode("first", "any")
				.back(new Helper.BackRecordBuilder().add("first", Helper.buildBackDataNode("first")).build())
				.frontDataNode("first")
				.frontMark("?")
				.autoComplete(99)
				.build();
		snooze = new Helper.TypeBuilder("snooze")
				.middleNode("value", "any")
				.back(new Helper.BackRecordBuilder().add("value", Helper.buildBackDataNode("value")).build())
				.frontMark("#")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		multiplier = new Helper.TypeBuilder("multiplier")
				.middlePrimitive("text")
				.middleNode("value", "any")
				.back(new Helper.BackRecordBuilder()
						.add("value", Helper.buildBackDataNode("value"))
						.add("text", Helper.buildBackDataPrimitive("text"))
						.build())
				.frontMark("x")
				.frontDataPrimitive("text")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		array = new Helper.TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontMark("[")
				.front(new Helper.FrontDataArrayBuilder("value")
						.addSeparator(new Helper.FrontMarkBuilder(", ").build())
						.build())
				.frontMark("]")
				.autoComplete(99)
				.build();
		record = new Helper.TypeBuilder("record")
				.middleRecord("value", "record_element")
				.back(Helper.buildBackDataRecord("value"))
				.frontMark("{")
				.frontDataArray("value")
				.frontMark("}")
				.autoComplete(99)
				.build();
		recordElement = new Helper.TypeBuilder("record_element")
				.middleRecordKey("key")
				.middleNode("value", "any")
				.back(Helper.buildBackDataKey("key"))
				.back(Helper.buildBackDataNode("value"))
				.frontDataPrimitive("key")
				.frontMark(": ")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		pair = new Helper.TypeBuilder("pair")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Helper.BackArrayBuilder()
						.add(Helper.buildBackDataNode("first"))
						.add(Helper.buildBackDataNode("second"))
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
				.back(new Helper.BackRecordBuilder()
						.add("first", Helper.buildBackDataPrimitive("first"))
						.add("second", Helper.buildBackDataPrimitive("second"))
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
