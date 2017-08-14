package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.helper.TreeBuilder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static com.zarbosoft.bonestruct.helper.Helper.buildDoc;
import static com.zarbosoft.bonestruct.helper.SyntaxLoadSave.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentSave {
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
	public void testTypedPrimitive() {
		check(new TreeBuilder(typedPrimitive), "(z)\"x\",");
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
	public void testDataArrayWithType() {
		check(new TreeBuilder(dataArray).addArray("value", new TreeBuilder(typedPrimitive).build()), "[(z)\"x\",],");
	}

	@Test
	public void testDataArrayWithTwoElements() {
		check(
				new TreeBuilder(dataArray).addArray("value",
						new TreeBuilder(typedPrimitive).build(),
						new TreeBuilder(primitive).build()
				),
				"[(z)\"x\",\"x\",],"
		);
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
				new TreeBuilder(dataRecordElement)
						.addKey("key", "cat")
						.add("value", new TreeBuilder(primitive).build())
						.build()
		), "{\"cat\":\"x\",},");
	}

	@Test
	public void testDataRecordWithType() {
		check(new TreeBuilder(dataRecord).addArray("value",
				new TreeBuilder(dataRecordElement)
						.addKey("key", "cat")
						.add("value", new TreeBuilder(typedPrimitive).build())
						.build()
		), "{\"cat\":(z)\"x\",},");
	}

	@Test
	public void testDataRecordWithTwoElements() {
		check(new TreeBuilder(dataRecord).addArray("value",
				new TreeBuilder(dataRecordElement)
						.addKey("key", "cat")
						.add("value", new TreeBuilder(typedPrimitive).build())
						.build(),
				new TreeBuilder(dataRecordElement)
						.addKey("key", "dog")
						.add("value", new TreeBuilder(primitive).build())
						.build()
		), "{\"cat\":(z)\"x\",\"dog\":\"x\",},");
	}
}
