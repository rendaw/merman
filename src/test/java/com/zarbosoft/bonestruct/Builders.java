package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.model.*;
import com.zarbosoft.bonestruct.editor.model.back.*;
import com.zarbosoft.bonestruct.editor.model.front.*;
import com.zarbosoft.bonestruct.editor.model.middle.*;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.rendaw.common.DeadCode;
import org.junit.ComparisonFailure;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static com.zarbosoft.rendaw.common.Common.zip;

public class Builders {
	public static void dump(final DataElement.Value value) {
		dump(value, new RawWriter(System.out, (byte) ' ', 4));
		System.out.write('\n');
		System.out.flush();
	}

	static void dump(final DataElement.Value value, final RawWriter writer) {
		uncheck(() -> {
			if (value.getClass() == DataArrayBase.Value.class) {
				writer.arrayBegin();
				((DataArrayBase.Value) value).get().stream().forEach(element -> dump(element, writer));
				writer.arrayEnd();
			} else if (value.getClass() == DataNode.Value.class) {
				dump(((DataNode.Value) value).get(), writer);
			} else if (value.getClass() == DataPrimitive.Value.class) {
				writer.quotedPrimitive(((DataPrimitive.Value) value).get().getBytes(StandardCharsets.UTF_8));
			} else
				throw new DeadCode();
		});
	}

	private static void dump(final Node value, final RawWriter writer) {
		uncheck(() -> {
			writer.type(value.type.id.getBytes(StandardCharsets.UTF_8));
			writer.recordBegin();
			value
					.dataKeys()
					.forEach(k -> dump(value.data(k), uncheck(() -> writer.key(k.getBytes(StandardCharsets.UTF_8)))));
			writer.recordEnd();
		});
	}

	static class SyntaxBuilder {

		private final Syntax syntax;

		public SyntaxBuilder(final String root) {
			this.syntax = new Syntax();
			syntax.root = new DataArray();
			syntax.root.type = root;
		}

		public SyntaxBuilder type(final FreeNodeType type) {
			syntax.types.add(type);
			return this;
		}

		public Syntax build() {
			syntax.finish();
			return syntax;
		}

		public SyntaxBuilder group(final String name, final Set<String> subtypes) {
			syntax.groups.put(name, subtypes);
			return this;
		}
	}

	static class GroupBuilder {
		Set<String> subtypes = new HashSet<>();

		public GroupBuilder type(final FreeNodeType type) {
			subtypes.add(type.id);
			return this;
		}

		public Set<String> build() {
			return subtypes;
		}

		public GroupBuilder group(final String group) {
			subtypes.add(group);
			return this;
		}
	}

	static class TypeBuilder {
		private final FreeNodeType type;

		public TypeBuilder(final String id) {
			this.type = new FreeNodeType();
			type.id = id;
			type.name = id;
			type.back = new ArrayList<>();
			type.middle = new HashMap<>();
			type.front = new ArrayList<>();
		}

		public TypeBuilder back(final BackPart back) {
			type.back.add(back);
			return this;
		}

		public FreeNodeType build() {
			return type;
		}

		public TypeBuilder front(final FrontPart front) {
			type.front.add(front);
			return this;
		}

		public TypeBuilder autoComplete(final int x) {
			type.autoChooseAmbiguity = x;
			return this;
		}

		public TypeBuilder frontDataNode(final String middle) {
			final FrontDataNode part = new FrontDataNode();
			part.middle = middle;
			type.front.add(part);
			return this;
		}

		public TypeBuilder frontDataArray(final String middle) {
			final FrontDataArray part = new FrontDataArray();
			part.middle = middle;
			type.front.add(part);
			return this;
		}

		public TypeBuilder frontDataPrimitive(final String middle) {
			final FrontDataPrimitive part = new FrontDataPrimitive();
			part.middle = middle;
			type.front.add(part);
			return this;
		}

		public TypeBuilder frontMark(final String value) {
			final FrontMark part = new FrontMark();
			part.value = value;
			type.front.add(part);
			return this;
		}

		public TypeBuilder middlePrimitive(final String id) {
			final DataPrimitive middle = new DataPrimitive();
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleNode(final String id, final String type) {
			final DataNode middle = new DataNode();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleArray(final String id, final String type) {
			final DataArray middle = new DataArray();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleRecord(final String id, final String type) {
			final DataRecord middle = new DataRecord();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleRecordKey(final String id) {
			final DataRecordKey middle = new DataRecordKey();
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder precedence(final int precedence) {
			this.type.precedence = precedence;
			return this;
		}

		public TypeBuilder associateAfter() {
			this.type.frontAssociative = false;
			return this;
		}

		public TypeBuilder associateBefore() {
			this.type.frontAssociative = true;
			return this;
		}
	}

	public static BackPart buildBackPrimitive(final String value) {
		final BackPrimitive back = new BackPrimitive();
		back.value = value;
		return back;
	}

	public static BackPart buildBackDataNode(final String middle) {
		final BackDataNode back = new BackDataNode();
		back.middle = middle;
		return back;
	}

	public static BackPart buildBackDataPrimitive(final String middle) {
		final BackDataPrimitive back = new BackDataPrimitive();
		back.middle = middle;
		return back;
	}

	public static BackPart buildBackDataRecord(final String middle) {
		final BackDataRecord back = new BackDataRecord();
		back.middle = middle;
		return back;
	}

	public static BackPart buildBackDataKey(final String middle) {
		final BackDataKey back = new BackDataKey();
		back.middle = middle;
		return back;
	}

	public static BackPart buildBackDataArray(final String middle) {
		final BackDataArray back = new BackDataArray();
		back.middle = middle;
		return back;
	}

	static class BackRecordBuilder {
		BackRecord back = new BackRecord();

		public BackRecordBuilder add(final String key, final BackPart part) {
			back.pairs.put(key, part);
			return this;
		}

		public BackPart build() {
			return back;
		}
	}

	static class BackArrayBuilder {
		BackArray back = new BackArray();

		public BackArrayBuilder add(final BackPart part) {
			back.elements.add(part);
			return this;
		}

		public BackPart build() {
			return back;
		}
	}

	public static class FrontMarkBuilder {
		private final FrontMark front;

		public FrontMarkBuilder(final String value) {
			this.front = new FrontMark();
			front.value = value;
		}

		public FrontMark build() {
			return front;
		}
	}

	public static class FrontDataArrayBuilder {
		private final FrontDataArray front;

		public FrontDataArrayBuilder(final String middle) {
			this.front = new FrontDataArray();
			front.middle = middle;
		}

		public FrontDataArrayBuilder addSeparator(final FrontConstantPart part) {
			front.separator.add(part);
			return this;
		}

		public FrontDataArray build() {
			return front;
		}
	}

	public static class TreeBuilder {
		private final NodeType type;
		private final Map<String, DataElement.Value> data = new HashMap<>();

		public TreeBuilder(final NodeType type) {
			this.type = type;
		}

		public TreeBuilder add(final String key, final TreeBuilder builder) {
			data.put(key, new DataNode.Value((DataNode) type.middle().get(key), builder.build()));
			return this;
		}

		public TreeBuilder add(final String key, final Node node) {
			data.put(key, new DataNode.Value((DataNode) type.middle().get(key), node));
			return this;
		}

		public TreeBuilder add(final String key, final String text) {
			data.put(key, new DataPrimitive.Value((DataPrimitive) type.middle().get(key), text));
			return this;
		}

		public TreeBuilder addKey(final String key, final String text) {
			data.put(key, new DataRecordKey.Value((DataRecordKey) type.middle().get(key), text));
			return this;
		}

		public TreeBuilder addArray(final String key, final List<Node> values) {
			data.put(key, new DataArray.Value(type.getDataArray(key), values));
			return this;
		}

		public TreeBuilder addArray(final String key, final Node... values) {
			data.put(key, new DataArray.Value(type.getDataArray(key), Arrays.asList(values)));
			return this;
		}

		public TreeBuilder addRecord(final String key, final Node... values) {
			data.put(key, new DataRecord.Value(type.getDataRecord(key), Arrays.asList(values)));
			return this;
		}

		public Node build() {
			return new Node(type, data);
		}

		public DataElement.Value buildArray() {
			return new DataArray.Value(null, Arrays.asList(new Node(type, data)));
		}

	}

	public static void assertTreeEqual(final Node expected, final Node got) {
		if (expected.type != got.type)
			throw new AssertionError(String.format("Node type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
					expected.type,
					got.type,
					got.getPath()
			));
		final Set<String> expectedKeys = expected.dataKeys();
		final Set<String> gotKeys = got.dataKeys();
		{
			final Set<String> missing = Sets.difference(expectedKeys, gotKeys);
			if (!missing.isEmpty())
				throw new AssertionError(String.format("Missing fields: %s\nAt: %s", missing, got.getPath()));
		}
		{
			final Set<String> extra = Sets.difference(gotKeys, expectedKeys);
			if (!extra.isEmpty())
				throw new AssertionError(String.format("Unknown fields: %s\nAt: %s", extra, got.getPath()));
		}
		for (final String key : Sets.intersection(expectedKeys, gotKeys)) {
			assertTreeEqual(expected.data(key), got.data(key));
		}
	}

	public static void assertTreeEqual(
			final DataElement.Value expected, final DataElement.Value got
	) {
		if (expected.getClass() == DataArrayBase.Value.class) {
			final DataArrayBase.Value expectedValue = (DataArrayBase.Value) expected;
			final DataArrayBase.Value gotValue = (DataArrayBase.Value) got;
			if (expectedValue.get().size() != gotValue.get().size())
				throw new AssertionError(String.format("Array length mismatch.\nExpected: %s\nGot: %s\nAt: %s",
						expectedValue.get().size(),
						gotValue.get().size(),
						got.getPath()
				));
			zip(expectedValue.get().stream(), gotValue.get().stream()).forEach(pair -> assertTreeEqual(pair.first,
					pair.second
			));
		} else if (expected.getClass() == DataNode.Value.class) {
			final DataNode.Value expectedValue = (DataNode.Value) expected;
			final DataNode.Value gotValue = (DataNode.Value) got;
			assertTreeEqual(expectedValue.get(), gotValue.get());
		} else if (expected.getClass() == DataPrimitive.Value.class) {
			final DataPrimitive.Value expectedValue = (DataPrimitive.Value) expected;
			final DataPrimitive.Value gotValue = (DataPrimitive.Value) got;
			if (!expectedValue.get().equals(gotValue.get()))
				throw new ComparisonFailure(String.format("Array length mismatch.\nAt: %s", got.getPath()),
						expectedValue.get(),
						gotValue.get()
				);
		} else
			throw new AssertionError(String.format("Node type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
					expected.getClass(),
					got.getClass(),
					got.getPath()
			));
	}

	public static void assertTreeEqual(
			final Context context, final Builders.TreeBuilder expected, final DataElement.Value got
	) {
		assertTreeEqual(new DataArrayBase.Value(context.syntax.root, ImmutableList.of(expected.build())), got);
	}

	public static Context buildDoc(final Syntax syntax, final Node... root) {
		final Document doc = syntax.create();
		final Context context = new Context(syntax, doc, null, null, null, new History());
		context.history.apply(context, new DataArrayBase.ChangeAdd(doc.top, 0, Arrays.asList(root)));
		final VisualNodePart visual =
				syntax.rootFront.createVisual(context, ImmutableMap.of("value", doc.top), ImmutableSet.of());
		visual.select(context);
		return context;
	}

}
