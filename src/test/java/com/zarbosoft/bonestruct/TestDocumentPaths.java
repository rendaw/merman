package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import static com.zarbosoft.bonestruct.Builders.TreeBuilder;
import static com.zarbosoft.bonestruct.Builders.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentPaths {
	@Test
	public void testRoot() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.quoted).add("value", "").build(),
				new TreeBuilder(MiscSyntax.multiback).add("a", "").add("b", "").build(),
				new TreeBuilder(MiscSyntax.quoted).add("value", "").build()
		);
		final Value value1 = context.document.top.get().get(0).data("value");
		assertThat(value1.getPath().toList(), equalTo(ImmutableList.of("0")));
		assertThat(context.locate(value1.getPath()), equalTo(value1));
		final Value value2 = context.document.top.get().get(1).data("b");
		assertThat(value2.getPath().toList(), equalTo(ImmutableList.of("2")));
		assertThat(context.locate(value2.getPath()), equalTo(value2));
		final Value value3 = context.document.top.get().get(2).data("value");
		assertThat(value3.getPath().toList(), equalTo(ImmutableList.of("3")));
		assertThat(context.locate(value3.getPath()), equalTo(value3));
	}

	@Test
	public void testRecord() {
		final Syntax syntax = new Builders.SyntaxBuilder("any")
				.type(new Builders.TypeBuilder("base")
						.back(new Builders.BackRecordBuilder().add("a", Builders.buildBackDataPrimitive("a")).build())
						.middlePrimitive("a")
						.frontDataPrimitive("a")
						.build())
				.group("any", ImmutableSet.of("base"))
				.build();
		final Context context = buildDoc(syntax, new Builders.TreeBuilder(syntax.types.get(0)).add("a", "").build());
		final Value value1 = context.document.top.get().get(0).data("a");
		assertThat(value1.getPath().toList(), equalTo(ImmutableList.of("0", "a")));
		assertThat(context.locate(value1.getPath()), equalTo(value1));
	}

	@Test
	public void testArray() {
		final Syntax syntax = new Builders.SyntaxBuilder("any")
				.type(new Builders.TypeBuilder("base")
						.back(new Builders.BackArrayBuilder().add(Builders.buildBackDataPrimitive("a")).build())
						.middlePrimitive("a")
						.frontDataPrimitive("a")
						.build())
				.group("any", ImmutableSet.of("base"))
				.build();
		final Context context = buildDoc(syntax, new Builders.TreeBuilder(syntax.types.get(0)).add("a", "").build());
		final Value value1 = context.document.top.get().get(0).data("a");
		assertThat(value1.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
		assertThat(context.locate(value1.getPath()), equalTo(value1));
	}

	@Test
	public void testDataNode() {
		final Syntax syntax = new Builders.SyntaxBuilder("any")
				.type(new Builders.TypeBuilder("base")
						.back(Builders.buildBackDataNode("a"))
						.middleNode("a", "child")
						.frontDataNode("a")
						.build())
				.type(new Builders.TypeBuilder("child")
						.back(Builders.buildBackDataPrimitive("b"))
						.middlePrimitive("b")
						.frontDataPrimitive("b")
						.build())
				.group("any", ImmutableSet.of("base"))
				.build();
		final Context context = buildDoc(syntax,
				new Builders.TreeBuilder(syntax.types.get(0))
						.add("a", new Builders.TreeBuilder(syntax.types.get(1)).add("b", ""))
						.build()
		);
		final Value value1 = ((ValueNode) context.document.top.get().get(0).data("a")).get().data("b");
		assertThat(value1.getPath().toList(), equalTo(ImmutableList.of("0")));
		assertThat(context.locate(value1.getPath()), equalTo(value1));
	}

	@Test
	public void testDataArray() {
		final Syntax syntax = new Builders.SyntaxBuilder("any")
				.type(new Builders.TypeBuilder("base")
						.back(Builders.buildBackDataArray("a"))
						.middleArray("a", "child")
						.frontDataArray("a")
						.build())
				.type(new Builders.TypeBuilder("child")
						.back(Builders.buildBackDataPrimitive("b"))
						.middlePrimitive("b")
						.frontDataPrimitive("b")
						.build())
				.group("any", ImmutableSet.of("base"))
				.build();
		final Context context = buildDoc(
				syntax,
				new Builders.TreeBuilder(syntax.types.get(0))
						.addArray("a",
								ImmutableList.of(new Builders.TreeBuilder(syntax.types.get(1)).add("b", "").build())
						)
						.build()
		);
		final Value value1 = ((ValueArray) context.document.top.get().get(0).data("a")).get().get(0).data("b");
		assertThat(value1.getPath().toList(), equalTo(ImmutableList.of("0", "0")));
		assertThat(context.locate(value1.getPath()), equalTo(value1));
	}

	@Test
	public void testDataRecord() {
		final Syntax syntax = new Builders.SyntaxBuilder("any")
				.type(new Builders.TypeBuilder("base")
						.back(Builders.buildBackDataRecord("a"))
						.middleRecord("a", "element")
						.frontDataArray("a")
						.build())
				.type(new Builders.TypeBuilder("element")
						.back(Builders.buildBackDataKey("k"))
						.middleRecordKey("k")
						.frontDataPrimitive("k")
						.back(Builders.buildBackDataPrimitive("v"))
						.middlePrimitive("v")
						.frontDataPrimitive("v")
						.build())
				.group("any", ImmutableSet.of("base"))
				.build();
		final Context context = buildDoc(
				syntax,
				new Builders.TreeBuilder(syntax.types.get(0))
						.addRecord("a",
								new Builders.TreeBuilder(syntax.types.get(1)).add("k", "K").add("v", "V").build()
						)
						.build()
		);
		final Value value1 = ((ValueArray) context.document.top.get().get(0).data("a")).get().get(0).data("v");
		assertThat(value1.getPath().toList(), equalTo(ImmutableList.of("0", "K")));
		assertThat(context.locate(value1.getPath()), equalTo(value1));
	}

	@Test
	public void testLocateRootElement() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new TreeBuilder(MiscSyntax.quoted).add("value", "").build(),
				new TreeBuilder(MiscSyntax.multiback).add("a", "").add("b", "").build(),
				new TreeBuilder(MiscSyntax.quoted).add("value", "").build()
		);
		final Path path0 = new Path("0");
		assertThat(context.locate(path0), equalTo(context.document.top.get().get(0).data("value")));
		final Path path1 = new Path("1");
		assertThat(context.locate(path1), equalTo(context.document.top.get().get(1).data("a")));
		final Path path2 = new Path("2");
		assertThat(context.locate(path2), equalTo(context.document.top.get().get(1).data("b")));
		final Path path3 = new Path("3");
		assertThat(context.locate(path3), equalTo(context.document.top.get().get(2).data("value")));
	}

	@Test
	public void testLocateEmpty() {
		final Context context = buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.one).build());
		assertThat(context.locate(new Path("0")), equalTo(context.document.top.get().get(0)));
	}

	@Test
	public void testLocatePrimitive() {
		final Context context =
				buildDoc(MiscSyntax.syntax, new TreeBuilder(MiscSyntax.quoted).add("value", "").build());
		assertThat(context.locate(new Path("0")), equalTo(context.document.top.get().get(0).data("value")));
	}

	@Test
	public void testLocateRecordNode() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.plus)
						.add("first", new Builders.TreeBuilder(MiscSyntax.one))
						.add("second", new Builders.TreeBuilder(MiscSyntax.one))
						.build()
		);
		assertThat(context.locate(new Path("0")), equalTo(context.document.top.get().get(0)));
		assertThat(context.locate(new Path("0", "first")),
				equalTo(((ValueNode) context.document.top.get().get(0).data("first")).get())
		);
		assertThat(context.locate(new Path("0", "second")),
				equalTo(((ValueNode) context.document.top.get().get(0).data("second")).get())
		);
	}

	@Test
	public void testLocateRecordPrimitive() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.ratio).add("first", "").add("second", "").build()
		);
		assertThat(context.locate(new Path("0", "first")), equalTo(context.document.top.get().get(0).data("first")));
		assertThat(context.locate(new Path("0", "second")), equalTo(context.document.top.get().get(0).data("second")));
	}

	@Test
	public void testLocateArrayElement() {
		final Context context = buildDoc(MiscSyntax.syntax,
				new Builders.TreeBuilder(MiscSyntax.pair)
						.add("first", new Builders.TreeBuilder(MiscSyntax.one))
						.add("second", new Builders.TreeBuilder(MiscSyntax.one))
						.build()
		);
		assertThat(context.locate(new Path("0", "0")),
				equalTo(((ValueNode) context.document.top.get().get(0).data("first")).get())
		);
		assertThat(context.locate(new Path("0", "1")),
				equalTo(((ValueNode) context.document.top.get().get(0).data("second")).get())
		);
	}

	@Test
	public void testLocateDataRecordElement() {
		final Context context = buildDoc(MiscSyntax.syntax, new Builders.TreeBuilder(MiscSyntax.record).addRecord(
				"value",
				new Builders.TreeBuilder(MiscSyntax.recordElement)
						.add("key", "first")
						.add("value", new TreeBuilder(MiscSyntax.one))
						.build(),
				new Builders.TreeBuilder(MiscSyntax.recordElement)
						.add("key", "second")
						.add("value", new TreeBuilder(MiscSyntax.one))
						.build()
		).build());
		assertThat(context.locate(new Path("0", "first")), equalTo((
				(ValueNode) ((ValueArray) context.document.top.get().get(0).data("value")).get().get(0).data("value")
		).get()));
		assertThat(context.locate(new Path("0", "second")), equalTo((
				(ValueNode) ((ValueArray) context.document.top.get().get(0).data("value")).get().get(1).data("value")
		).get()));
	}

	@Test
	public void testLocateDataArrayElement() {
		final Context context = buildDoc(MiscSyntax.syntax, new Builders.TreeBuilder(MiscSyntax.array)
				.addArray("value",
						new TreeBuilder(MiscSyntax.one).build(),
						new TreeBuilder(MiscSyntax.one).build()
				)
				.build());
		assertThat(context.locate(new Path("0", "0")),
				equalTo(((ValueArray) context.document.top.get().get(0).data("value")).get().get(0))
		);
		assertThat(context.locate(new Path("0", "1")),
				equalTo(((ValueArray) context.document.top.get().get(0).data("value")).get().get(1))
		);
	}
}
