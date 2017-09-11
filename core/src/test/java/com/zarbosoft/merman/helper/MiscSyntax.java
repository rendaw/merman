package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;

public class MiscSyntax {
	final public static FreeAtomType infinity;
	final public static FreeAtomType one;
	final public static FreeAtomType two;
	final public static FreeAtomType three;
	final public static FreeAtomType four;
	final public static FreeAtomType five;
	final public static FreeAtomType seven;
	final public static FreeAtomType multiback;
	final public static FreeAtomType quoted;
	final public static FreeAtomType digits;
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
	final public static FreeAtomType restricted;
	final public static FreeAtomType restrictedArray;
	final public static Syntax syntax;

	static {
		infinity = new TypeBuilder("infinity")
				.back(Helper.buildBackPrimitive("infinity"))
				.front(new FrontMarkBuilder("infinity").build())
				.autoComplete(1)
				.build();
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.autoComplete(-1)
				.build();
		two = new TypeBuilder("two")
				.back(Helper.buildBackPrimitive("two"))
				.front(new FrontMarkBuilder("two").build())
				.autoComplete(-1)
				.build();
		three = new TypeBuilder("three")
				.back(Helper.buildBackPrimitive("three"))
				.front(new FrontMarkBuilder("three").build())
				.autoComplete(-1)
				.build();
		four = new TypeBuilder("four")
				.back(Helper.buildBackPrimitive("four"))
				.front(new FrontMarkBuilder("four").build())
				.autoComplete(-1)
				.build();
		five = new TypeBuilder("five")
				.back(Helper.buildBackPrimitive("five"))
				.front(new FrontMarkBuilder("five").build())
				.autoComplete(-1)
				.build();
		seven = new TypeBuilder("seven")
				.back(Helper.buildBackPrimitive("7"))
				.front(new FrontMarkBuilder("7").build())
				.autoComplete(-1)
				.build();
		multiback = new TypeBuilder("multiback")
				.back(Helper.buildBackDataPrimitive("a"))
				.back(Helper.buildBackDataPrimitive("b"))
				.middlePrimitive("a")
				.middlePrimitive("b")
				.frontDataPrimitive("a")
				.frontMark("^")
				.frontDataPrimitive("b")
				.autoComplete(-1)
				.build();
		quoted = new TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.front(new FrontMarkBuilder("\"").build())
				.frontDataPrimitive("value")
				.front(new FrontMarkBuilder("\"").build())
				.autoComplete(1)
				.build();
		digits = new TypeBuilder("digits")
				.middlePrimitiveDigits("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.autoComplete(1)
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
				.autoComplete(-1)
				.build();
		plus = new TypeBuilder("plus")
				.middleAtom("first", "any")
				.middleAtom("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataAtom("first"))
						.add("second", Helper.buildBackDataAtom("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+")
				.frontDataNode("second")
				.autoComplete(-1)
				.build();
		plusEqual = new TypeBuilder("plusequal")
				.middleAtom("first", "any")
				.middleAtom("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataAtom("first"))
						.add("second", Helper.buildBackDataAtom("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+=")
				.frontDataNode("second")
				.autoComplete(-1)
				.build();
		binaryBang = new TypeBuilder("bang")
				.middleAtom("first", "any")
				.middleAtom("second", "any")
				.back(new BackRecordBuilder()
						.add("first", Helper.buildBackDataAtom("first"))
						.add("second", Helper.buildBackDataAtom("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.autoComplete(1)
				.build();
		waddle = new TypeBuilder("waddle")
				.middleAtom("first", "any")
				.back(new BackRecordBuilder().add("first", Helper.buildBackDataAtom("first")).build())
				.frontDataNode("first")
				.frontMark("?")
				.autoComplete(1)
				.build();
		snooze = new TypeBuilder("snooze")
				.middleAtom("value", "any")
				.back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
				.frontMark("#")
				.frontDataNode("value")
				.autoComplete(1)
				.build();
		multiplier = new TypeBuilder("multiplier")
				.middlePrimitive("text")
				.middleAtom("value", "any")
				.back(new BackRecordBuilder()
						.add("value", Helper.buildBackDataAtom("value"))
						.add("text", Helper.buildBackDataPrimitive("text"))
						.build())
				.frontMark("x")
				.frontDataPrimitive("text")
				.frontDataNode("value")
				.autoComplete(1)
				.build();
		array = new TypeBuilder("array")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontMark("[")
				.front(new FrontDataArrayBuilder("value").addSeparator(new FrontMarkBuilder(", ").build()).build())
				.frontMark("]")
				.autoComplete(1)
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
				.autoComplete(1)
				.build();
		recordElement = new TypeBuilder("record_element")
				.middlePrimitive("key")
				.middleAtom("value", "any")
				.back(Helper.buildBackDataKey("key"))
				.back(Helper.buildBackDataAtom("value"))
				.frontDataPrimitive("key")
				.frontMark(": ")
				.frontDataNode("value")
				.autoComplete(1)
				.build();
		pair = new TypeBuilder("pair")
				.middleAtom("first", "any")
				.middleAtom("second", "any")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackDataAtom("first"))
						.add(Helper.buildBackDataAtom("second"))
						.build())
				.frontMark("<")
				.frontDataNode("first")
				.frontMark(", ")
				.frontDataNode("second")
				.frontMark(">")
				.autoComplete(1)
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
		restricted = new TypeBuilder("restricted")
				.middleAtom("value", "restricted_group")
				.back(new BackRecordBuilder().add("value", Helper.buildBackDataAtom("value")).build())
				.frontDataNode("value")
				.build();
		restrictedArray = new TypeBuilder("restricted_array")
				.middleArray("value", "restricted_array_group")
				.back(Helper.buildBackDataArray("value"))
				.frontMark("_")
				.front(new FrontDataArrayBuilder("value").build())
				.autoComplete(1)
				.build();
		syntax = new SyntaxBuilder("any")
				.type(infinity)
				.type(one)
				.type(two)
				.type(three)
				.type(four)
				.type(five)
				.type(seven)
				.type(multiback)
				.type(quoted)
				.type(digits)
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
				.type(restricted)
				.type(restrictedArray)
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
				.group("restricted_group", new GroupBuilder().type(quoted).build())
				.group("restricted_array_group", new GroupBuilder().type(quoted).build())
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
								.type(digits)
								.type(seven)
								.type(plus)
								.type(plusEqual)
								.type(binaryBang)
								.type(waddle)
								.type(snooze)
								.type(multiplier)
								.type(array)
								.type(restrictedArray)
								.type(record)
								.type(pair)
								.type(ratio)
								.build()
				)
				.group("arrayChildren", new GroupBuilder().type(one).type(MiscSyntax.multiback).build())
				.build();
	}
}
