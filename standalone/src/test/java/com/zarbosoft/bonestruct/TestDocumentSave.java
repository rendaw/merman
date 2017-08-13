package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.helper.*;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static com.zarbosoft.bonestruct.helper.Helper.buildDoc;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentSave {
	final static FreeAtomType primitive;
	final static FreeAtomType doublePrimitive;
	final static FreeAtomType array;
	final static FreeAtomType record;
	final static FreeAtomType dataPrimitive;
	final static FreeAtomType dataArray;
	final static FreeAtomType dataRecord;
	final static FreeAtomType dataRecordElement;
	static Syntax syntax;

	static {
		primitive = new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
		doublePrimitive = new TypeBuilder("doublePrimitive")
				.back(Helper.buildBackPrimitive("x"))
				.back(Helper.buildBackPrimitive("y"))
				.frontMark("x")
				.build();
		array = new TypeBuilder("array")
				.back(new BackArrayBuilder()
						.add(Helper.buildBackPrimitive("x"))
						.add(Helper.buildBackPrimitive("y"))
						.build())
				.frontMark("x")
				.build();
		record = new TypeBuilder("record")
				.back(new BackRecordBuilder()
						.add("a", Helper.buildBackPrimitive("x"))
						.add("b", Helper.buildBackPrimitive("y"))
						.build())
				.frontMark("x")
				.build();
		dataPrimitive = new TypeBuilder("dataPrimitive")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		dataArray = new TypeBuilder("dataArray")
				.middleArray("value", "any")
				.back(Helper.buildBackDataArray("value"))
				.frontDataArray("value")
				.build();
		dataRecord = new TypeBuilder("dataRecord")
				.middleRecord("value", "dataRecordElement")
				.back(Helper.buildBackDataRecord("value"))
				.frontDataArray("value")
				.build();
		dataRecordElement = new TypeBuilder("dataRecordElement")
				.middleRecordKey("key")
				.back(Helper.buildBackDataKey("key"))
				.back(Helper.buildBackPrimitive("value"))
				.frontDataPrimitive("key")
				.build();
		syntax = new SyntaxBuilder("any")
				.type(primitive)
				.type(doublePrimitive)
				.type(array)
				.type(record)
				.type(dataPrimitive)
				.type(dataArray)
				.type(dataRecord)
				.type(dataRecordElement)
				.group("any",
						new GroupBuilder()
								.type(primitive)
								.type(doublePrimitive)
								.type(array)
								.type(record)
								.type(dataPrimitive)
								.type(dataArray)
								.type(dataRecord)
								.type(dataRecordElement)
								.build()
				)
				.build();
	}

	public void check(final TreeBuilder tree, final String result) {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		buildDoc(syntax, tree.build()).document.write(stream);
		assertThat(new String(stream.toByteArray(), StandardCharsets.UTF_8), equalTo(result));
	}

	@Test
	public void testPrimitive() {
		check(new TreeBuilder(primitive), "\"x\",");
	}

	@Test
	public void testDoublePrimitive() {
		check(new TreeBuilder(doublePrimitive), "\"x\",\"y\",");
	}

	@Test
	public void testArray() {
		check(new TreeBuilder(array), "[\"x\",\"y\",],");
	}

	@Test
	public void testRecord() {
		check(new TreeBuilder(record), "{\"a\":\"x\",\"b\":\"y\",},");
	}

	@Test
	public void testDataPrimitive() {
		check(new TreeBuilder(dataPrimitive).add("value", "dog"), "\"dog\",");
	}

	@Test
	public void testDataArray() {
		check(new TreeBuilder(dataArray).addArray("value", new TreeBuilder(primitive).build()), "[\"x\",],");
	}

	@Test
	public void testDataArrayDouble() {
		check(
				new TreeBuilder(dataArray).addArray("value", new TreeBuilder(doublePrimitive).build()),
				"[\"x\",\"y\",],"
		);
	}

	@Test
	public void testDataRecord() {
		check(new TreeBuilder(dataRecord).addArray("value",
				new TreeBuilder(dataRecordElement).addKey("key", "cat").build()
		), "{\"cat\":\"value\",},");
	}
}
