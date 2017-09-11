package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;

public class SyntaxLoadSave {
	public final static FreeAtomType primitive;
	public final static FreeAtomType typedPrimitive;
	public final static FreeAtomType doublePrimitive;
	public final static FreeAtomType array;
	public final static FreeAtomType record;
	public final static FreeAtomType dataPrimitive;
	public final static FreeAtomType dataArray;
	public final static FreeAtomType dataRecord;
	public final static FreeAtomType dataRecordElement;
	public final static Syntax syntax;

	static {
		primitive = new TypeBuilder("primitive").back(Helper.buildBackPrimitive("x")).frontMark("x").build();
		typedPrimitive = new TypeBuilder("typedPrimitive")
				.back(Helper.buildBackType("z", Helper.buildBackPrimitive("x")))
				.frontMark("x")
				.build();
		doublePrimitive = new TypeBuilder("doublePrimitive")
				.back(Helper.buildBackPrimitive("x"))
				.back(Helper.buildBackPrimitive("y"))
				.frontMark("x")
				.build();
		array = new TypeBuilder("array").back(Helper.buildBackType(
				"typedArray",
				new BackArrayBuilder().add(Helper.buildBackPrimitive("x")).add(Helper.buildBackPrimitive("y")).build()
		)).frontMark("x").build();
		record = new TypeBuilder("record").back(Helper.buildBackType(
				"typedRecord",
				new BackRecordBuilder()
						.add("a", Helper.buildBackPrimitive("x"))
						.add("b", Helper.buildBackPrimitive("y"))
						.build()
		)).frontMark("x").build();
		dataPrimitive = new TypeBuilder("dataPrimitive")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		dataArray = new TypeBuilder("dataArray")
				.middleArray("value", "array_value")
				.back(Helper.buildBackDataArray("value"))
				.frontDataArray("value")
				.build();
		dataRecord = new TypeBuilder("dataRecord")
				.middleRecord("value", "dataRecordElement")
				.back(Helper.buildBackDataRecord("value"))
				.frontDataArray("value")
				.build();
		dataRecordElement = new TypeBuilder("dataRecordElement")
				.middlePrimitive("key")
				.middleAtom("value", "value")
				.back(Helper.buildBackDataKey("key"))
				.back(Helper.buildBackDataAtom("value"))
				.frontDataPrimitive("key")
				.frontDataNode("value")
				.build();
		syntax = new SyntaxBuilder("array_value")
				.type(primitive)
				.type(typedPrimitive)
				.type(doublePrimitive)
				.type(array)
				.type(record)
				.type(dataPrimitive)
				.type(dataArray)
				.type(dataRecord)
				.type(dataRecordElement)
				.group(
						"array_value",
						new GroupBuilder()
								.type(primitive)
								.type(typedPrimitive)
								.type(doublePrimitive)
								.type(array)
								.type(record)
								.type(dataPrimitive)
								.type(dataArray)
								.type(dataRecord)
								.build()
				)
				.group(
						"value",
						new GroupBuilder()
								.type(primitive)
								.type(typedPrimitive)
								.type(array)
								.type(record)
								.type(dataPrimitive)
								.type(dataArray)
								.type(dataRecord)
								.build()
				)
				.build();
	}

}
