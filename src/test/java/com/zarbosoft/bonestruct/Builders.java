package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.display.MockeryDisplay;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.ClipboardEngine;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.history.History;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.*;
import com.zarbosoft.bonestruct.syntax.middle.*;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.rendaw.common.DeadCode;
import org.junit.ComparisonFailure;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static com.zarbosoft.rendaw.common.Common.zip;

public class Builders {
	public static void dump(final Value value, final RawWriter writer) {
		uncheck(() -> {
			if (value.getClass() == ValueArray.class) {
				writer.arrayBegin();
				((ValueArray) value).get().stream().forEach(element -> dump(element, writer));
				writer.arrayEnd();
			} else if (value.getClass() == ValueNode.class) {
				dump(((ValueNode) value).get(), writer);
			} else if (value.getClass() == ValuePrimitive.class) {
				writer.quotedPrimitive(((ValuePrimitive) value).get().getBytes(StandardCharsets.UTF_8));
			} else
				throw new DeadCode();
		});
	}

	private static void dump(final Node value, final RawWriter writer) {
		uncheck(() -> {
			writer.type(value.type.id.getBytes(StandardCharsets.UTF_8));
			writer.recordBegin();
			value.data
					.keySet()
					.forEach(k -> dump(value.data.get(k),
							uncheck(() -> writer.key(k.getBytes(StandardCharsets.UTF_8)))
					));
			writer.recordEnd();
		});
	}

	public static void dump(final Value value) {
		dump(value, new RawWriter(System.out, (byte) ' ', 4));
		System.out.write('\n');
		System.out.flush();
	}

	static class SyntaxBuilder {

		private final Syntax syntax;

		public SyntaxBuilder(final String root) {
			this.syntax = new Syntax();
			syntax.root = new MiddleArray();
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
			final MiddlePrimitive middle = new MiddlePrimitive();
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleNode(final String id, final String type) {
			final MiddleNode middle = new MiddleNode();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleArray(final String id, final String type) {
			final MiddleArray middle = new MiddleArray();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleRecord(final String id, final String type) {
			final MiddleRecord middle = new MiddleRecord();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
			return this;
		}

		public TypeBuilder middleRecordKey(final String id) {
			final MiddleRecordKey middle = new MiddleRecordKey();
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
		private final Map<String, Value> data = new HashMap<>();

		public TreeBuilder(final NodeType type) {
			this.type = type;
		}

		public TreeBuilder add(final String key, final TreeBuilder builder) {
			data.put(key, new ValueNode((MiddleNode) type.middle().get(key), builder.build()));
			return this;
		}

		public TreeBuilder add(final String key, final Node node) {
			data.put(key, new ValueNode((MiddleNode) type.middle().get(key), node));
			return this;
		}

		public TreeBuilder add(final String key, final String text) {
			data.put(key, new ValuePrimitive((MiddlePrimitive) type.middle().get(key), text));
			return this;
		}

		public TreeBuilder addKey(final String key, final String text) {
			data.put(key, new ValuePrimitive((MiddleRecordKey) type.middle().get(key), text));
			return this;
		}

		public TreeBuilder addArray(final String key, final List<Node> values) {
			data.put(key, new ValueArray(type.getDataArray(key), values));
			return this;
		}

		public TreeBuilder addArray(final String key, final Node... values) {
			data.put(key, new ValueArray(type.getDataArray(key), Arrays.asList(values)));
			return this;
		}

		public TreeBuilder addRecord(final String key, final Node... values) {
			data.put(key, new ValueArray(type.getDataRecord(key), Arrays.asList(values)));
			return this;
		}

		public Node build() {
			return new Node(type, data);
		}

		public Value buildArray() {
			return new ValueArray(null, Arrays.asList(new Node(type, data)));
		}

	}

	public static void assertTreeEqual(final Node expected, final Node got) {
		if (expected.type != got.type)
			throw new AssertionError(String.format("Node type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
					expected.type,
					got.type,
					got.getPath()
			));
		final Set<String> expectedKeys = expected.data.keySet();
		final Set<String> gotKeys = got.data.keySet();
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
			assertTreeEqual(expected.data.get(key), got.data.get(key));
		}
	}

	public static void assertTreeEqual(
			final Value expected, final Value got
	) {
		if (expected.getClass() == ValueArray.class) {
			final ValueArray expectedValue = (ValueArray) expected;
			final ValueArray gotValue = (ValueArray) got;
			if (expectedValue.get().size() != gotValue.get().size())
				throw new AssertionError(String.format("Array length mismatch.\nExpected: %s\nGot: %s\nAt: %s",
						expectedValue.get().size(),
						gotValue.get().size(),
						got.getPath()
				));
			zip(expectedValue.get().stream(), gotValue.get().stream()).forEach(pair -> assertTreeEqual(pair.first,
					pair.second
			));
		} else if (expected.getClass() == ValueNode.class) {
			final ValueNode expectedValue = (ValueNode) expected;
			final ValueNode gotValue = (ValueNode) got;
			assertTreeEqual(expectedValue.get(), gotValue.get());
		} else if (expected.getClass() == ValuePrimitive.class) {
			final ValuePrimitive expectedValue = (ValuePrimitive) expected;
			final ValuePrimitive gotValue = (ValuePrimitive) got;
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
			final Context context, final Node expected, final Value got
	) {
		assertTreeEqual(new ValueArray(context.syntax.root, ImmutableList.of(expected)), got);
	}

	public static Context buildDoc(final Syntax syntax, final Node... root) {
		final Document doc = new Document(syntax, new ValueArray(syntax.root, Arrays.asList(root)));
		final Context context = new Context(syntax, doc, new MockeryDisplay(), idleTask -> {
		}, new History());
		context.clipboardEngine = new ClipboardEngine() {
			byte[] data = null;

			@Override
			public void set(final byte[] bytes) {
				data = bytes;
			}

			@Override
			public byte[] get() {
				return data;
			}
		};
		final Node rootNode = new Node(ImmutableMap.of("value", doc.top));
		final VisualPart visual = syntax.rootFront.createVisual(context, rootNode, ImmutableSet.of());
		visual.selectDown(context);
		return context;
	}

}
