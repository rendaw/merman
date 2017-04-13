package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;

import static com.zarbosoft.bonestruct.Builders.*;

public class MiscSyntax {
	final static FreeNodeType infinity;
	final static FreeNodeType one;
	final static FreeNodeType multiback;
	final static FreeNodeType quoted;
	final static FreeNodeType binaryBang;
	final static FreeNodeType plusEqual;
	final static FreeNodeType plus;
	final static FreeNodeType waddle;
	final static FreeNodeType snooze;
	final static FreeNodeType multiplier;
	final static FreeNodeType array;
	final static FreeNodeType openArray;
	final static FreeNodeType record;
	final static FreeNodeType recordElement;
	final static FreeNodeType openRecord;
	final static FreeNodeType pair;
	final static FreeNodeType ratio;
	final static Syntax syntax;

	static {
		infinity = new Builders.TypeBuilder("infinity")
				.back(Builders.buildBackPrimitive("infinity"))
				.front(new Builders.FrontMarkBuilder("infinity").build())
				.autoComplete(99)
				.build();
		one = new Builders.TypeBuilder("one")
				.back(Builders.buildBackPrimitive("one"))
				.front(new Builders.FrontMarkBuilder("one").build())
				.build();
		multiback = new Builders.TypeBuilder("multiback")
				.back(buildBackDataPrimitive("a"))
				.back(buildBackDataPrimitive("b"))
				.middlePrimitive("a")
				.middlePrimitive("b")
				.frontDataPrimitive("a")
				.frontMark("^")
				.frontDataPrimitive("b")
				.build();
		quoted = new Builders.TypeBuilder("quoted")
				.middlePrimitive("value")
				.back(buildBackDataPrimitive("value"))
				.front(new Builders.FrontMarkBuilder("\"").build())
				.frontDataPrimitive("value")
				.front(new Builders.FrontMarkBuilder("\"").build())
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
				.build();
		plusEqual = new Builders.TypeBuilder("plusequal")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("+=")
				.frontDataNode("second")
				.build();
		binaryBang = new Builders.TypeBuilder("bang")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackRecordBuilder()
						.add("first", buildBackDataNode("first"))
						.add("second", buildBackDataNode("second"))
						.build())
				.frontDataNode("first")
				.frontMark("!")
				.frontDataNode("second")
				.autoComplete(99)
				.build();
		waddle = new Builders.TypeBuilder("waddle")
				.middleNode("first", "any")
				.back(new Builders.BackRecordBuilder().add("first", buildBackDataNode("first")).build())
				.frontDataNode("first")
				.frontMark("?")
				.autoComplete(99)
				.build();
		snooze = new Builders.TypeBuilder("snooze")
				.middleNode("value", "any")
				.back(new Builders.BackRecordBuilder().add("value", buildBackDataNode("value")).build())
				.frontMark("#")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		multiplier = new Builders.TypeBuilder("multiplier")
				.middlePrimitive("text")
				.middleNode("value", "any")
				.back(new Builders.BackRecordBuilder()
						.add("value", buildBackDataNode("value"))
						.add("text", buildBackDataPrimitive("text"))
						.build())
				.frontMark("x")
				.frontDataPrimitive("text")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		array = new Builders.TypeBuilder("array")
				.middleArray("value", "any")
				.back(buildBackDataArray("value"))
				.frontMark("<")
				.front(new FrontDataArrayBuilder("value").addSeparator(new FrontMarkBuilder(", ").build()).build())
				.frontMark(">")
				.autoComplete(99)
				.build();
		openArray = new Builders.TypeBuilder("open_array")
				.middleArray("value", "any")
				.back(buildBackDataArray("value"))
				.front(new FrontDataArrayBuilder("value").addSeparator(new FrontMarkBuilder(",,").build()).build())
				.frontDataArray("value")
				.autoComplete(99)
				.build();
		record = new Builders.TypeBuilder("record")
				.middleRecord("value", "record_element")
				.back(buildBackDataRecord("value"))
				.frontMark("{")
				.frontDataArray("value")
				.frontMark("}")
				.autoComplete(99)
				.build();
		recordElement = new Builders.TypeBuilder("record_element")
				.middleRecordKey("key")
				.middleNode("value", "any")
				.back(buildBackDataKey("key"))
				.back(buildBackDataNode("value"))
				.frontDataPrimitive("key")
				.frontMark(": ")
				.frontDataNode("value")
				.autoComplete(99)
				.build();
		openRecord = new Builders.TypeBuilder("open_record")
				.middleRecord("value", "record_element")
				.back(buildBackDataRecord("value"))
				.front(new Builders.FrontDataArrayBuilder("value")
						.addSeparator(new Builders.FrontMarkBuilder(",,").build())
						.build())
				.autoComplete(99)
				.build();
		pair = new Builders.TypeBuilder("pair")
				.middleNode("first", "any")
				.middleNode("second", "any")
				.back(new Builders.BackArrayBuilder()
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
		ratio = new Builders.TypeBuilder("ratio")
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
		syntax = new Builders.SyntaxBuilder("any")
				.type(infinity)
				.type(one)
				.type(multiback)
				.type(quoted)
				.type(plus)
				.type(plusEqual)
				.type(binaryBang)
				.type(waddle)
				.type(snooze)
				.type(multiplier)
				.type(array)
				.type(openArray)
				.type(record)
				.type(recordElement)
				.type(openRecord)
				.type(pair)
				.type(ratio)
				.group(
						"test_group_1",
						new Builders.GroupBuilder()
								.type(infinity)
								.type(MiscSyntax.one)
								.type(MiscSyntax.multiback)
								.group("test_group_2")
								.build()
				)
				.group("test_group_2", new Builders.GroupBuilder().type(quoted).build())
				.group(
						"any",
						new Builders.GroupBuilder()
								.type(infinity)
								.type(one)
								.type(quoted)
								.type(plus)
								.type(plusEqual)
								.type(binaryBang)
								.type(waddle)
								.type(snooze)
								.type(multiplier)
								.type(array)
								.type(openArray)
								.type(record)
								.type(openRecord)
								.type(pair)
								.type(ratio)
								.build()
				)
				.group("arrayChildren", new Builders.GroupBuilder().type(one).type(MiscSyntax.multiback).build())
				.build();
	}
}
