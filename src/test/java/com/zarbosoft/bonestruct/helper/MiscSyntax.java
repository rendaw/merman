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
	final public static FreeAtomType doubleQuoted;
	final public static FreeAtomType binaryBang;
	final public static FreeAtomType plusEqual;
	final public static FreeAtomType plus;
	final public static FreeAtomType waddle;
	final public static FreeAtomType snooze;
	final public static FreeAtomType multiplier;
	final public static FreeAtomType array;
	final public static FreeAtomType doubleArray;
	final public static FreeAtomType record;
	final public static FreeAtomType recordElement;
	final public static FreeAtomType pair;
	final public static FreeAtomType ratio;
	final public static Syntax syntax;

	static {
		infinity = new TypeBuilder("infinity")
				.back(Helper.buildBackPrimitive("infinity"))
				.front(new FrontMarkBuilder("infinity").build())
				.autoComplete(99)
				.build();
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.build();
		two = new TypeBuilder("two")
				.back(Helper.buildBackPrimitive("two"))
				.front(new FrontMarkBuilder("two").build())
				.build();
		three = new TypeBuilder("three")
				.back(Helper.buildBackPrimitive("three"))
				.front(new FrontMarkBuilder("three").build())
				.build();
		four = new TypeBuilder("four")
				.back(Helper.buildBackPrimitive("four"))
				.front(new FrontMarkBuilder("four").build())
				.build();
		five = new TypeBuilder("five")
				.back(Helper.buildBackPrimitive("five"))
				.front(new FrontMarkBuilder("five").build())
				.build();
		multiback = new TypeBuilder("multiback")
				.back(Helper.buildBackDataPrimitive("a"))
				.back(Helper.buildBackDataPrimitive("b"))
				.middlePrimitive("a")
				.middlePrimitive("b")
				.frontDataPrimitive("a")
				.frontMark("^")
				.frontDataPrimitive("b")
				.build();
		quoted = new TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.front(new FrontMarkBuilder("\"").build())
				.frontDataPrimitive("value")
				.front(new FrontMarkBuilder("\"").build())
				.autoComplete(99)
				.build();
		doubleQuoted = new TypeBuilder("doubleuoted")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataPrimitive("first"))
						.add("second", Helper.buildBackDataPrimitive("second"))
						.build())
				.front(new FrontMarkBuilder("\"").build())
				.frontDataPrimitive("first")
				.front(new FrontMarkBuilder("\"").build())
				.frontDataPrimitive("second")
				.front(new FrontMarkBuilder("\"").build())
				.build();
		plus = new TypeBuilder("plus")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+")
				.frontDataNode("second")
				.build();
		plusEqual = new TypeBuilder("plusequal")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+=")
				.frontDataNode("second")
				.build();
		binaryBang = new TypeBuilder("bang")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataNode("first"))
						.add("second", Helper.buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.autoComplete(99)
				.build();
		waddle = new TypeBuilder("waddle")
				.middleNode("first", "any")
				.back(new BackRecordBuilder().add("first", Helper.buildBackDataNode("first")).build())
				.frontDataNode("first")
				.frontMark("?")
				.autoComplete(99)
				.build();
		snooze = new TypeBuilder("snooze")
				.middleNode("value", "any")
				.back(new BackRecordBuilder().add("value", Helper.buildBackDataNode("value")).build())
				.frontMark("#")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		multiplier = new TypeBuilder("multiplier")
				.middlePrimitive("text")
				.middleNode("value", "any")
				.back(new BackRecordBuilder()
						.add("value", Helper.buildBackDataNode("value"))
						.add("text", Helper.buildBackDataPrimitive("text"))
						.build())
				.frontMark("x")
				.frontDataPrimitive("text")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		array = new TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontMark("[")
				.front(new FrontDataArrayBuilder("value").addSeparator(new FrontMarkBuilder(", ").build()).build())
				.frontMark("]")
				.autoComplete(99)
				.build();
		doubleArray = new TypeBuilder("doublearray")
				.middleArray("first", "any")
				.middleArray("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataArray("first"))
						.add("second", Helper.buildBackDataArray("second"))
						.build())
				.frontMark("[")
				.frontDataArray("first")
				.frontMark("?")
				.frontDataArray("second")
				.frontMark("]")
				.build();
		record = new TypeBuilder("record")
				.middleRecord("value", "record_element")
				.back(Helper.buildBackDataRecord("value"))
				.frontMark("{")
				.frontDataArray("value")
				.frontMark("}")
				.autoComplete(99)
				.build();
		recordElement = new TypeBuilder("record_element")
				.middleRecordKey("key")
				.middleNode("value", "any")
				.back(Helper.buildBackDataKey("key"))
				.back(Helper.buildBackDataNode("value"))
				.frontDataPrimitive("key")
				.frontMark(": ")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		pair = new TypeBuilder("pair")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new BackArrayBuilder()
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
		ratio = new TypeBuilder("ratio")
				.middlePrimitive("first")
				.middlePrimitive("second")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataPrimitive("first"))
						.add("second", Helper.buildBackDataPrimitive("second"))
						.build())
				.frontMark("<")
				.frontDataPrimitive("first")
				.frontMark(":")
				.frontDataPrimitive("second")
				.frontMark(">")
				.build();
		syntax = new SyntaxBuilder("any")
				.type(infinity)
				.type(one)
				.type(two)
				.type(three)
				.type(four)
				.type(five)
				.type(multiback)
				.type(quoted)
				.type(doubleQuoted)
				.type(plus)
				.type(plusEqual)
				.type(binaryBang)
				.type(waddle)
				.type(snooze)
				.type(multiplier)
				.type(array)
				.type(doubleArray)
				.type(record)
				.type(recordElement)
				.type(pair)
				.type(ratio)
				.group(
						"test_group_1",
						new GroupBuilder()
								.type(infinity)
								.type(MiscSyntax.one)
								.type(MiscSyntax.multiback)
								.group("test_group_2")
								.build()
				)
				.group("test_group_2", new GroupBuilder().type(quoted).build())
				.group(
						"any",
						new GroupBuilder()
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
				.group("arrayChildren", new GroupBuilder().type(one).type(MiscSyntax.multiback).build())
				.build();
	}
}
