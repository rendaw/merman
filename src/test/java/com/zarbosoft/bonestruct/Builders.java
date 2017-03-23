package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.back.BackDataNode;
import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.back.BackPrimitive;
import com.zarbosoft.bonestruct.editor.model.back.BackRecord;
import com.zarbosoft.bonestruct.editor.model.front.FrontDataNode;
import com.zarbosoft.bonestruct.editor.model.front.FrontMark;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.*;
import com.zarbosoft.bonestruct.editor.visual.Context;
import org.junit.ComparisonFailure;

import java.util.*;

import static com.zarbosoft.rendaw.common.Common.zip;

public class Builders {
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

		public TypeBuilder immediate() {
			type.immediateMatch = true;
			return this;
		}

		public TypeBuilder frontDataNode(final String middle) {
			final FrontDataNode part = new FrontDataNode();
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

		public TypeBuilder middleNode(final String id, final String type) {
			final DataNode middle = new DataNode();
			middle.type = type;
			middle.id = id;
			this.type.middle.put(id, middle);
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

	public static class TreeBuilder {
		Node node;

		public TreeBuilder(final NodeType type) {
			node = new Node(type, new HashMap<>());
		}

		public TreeBuilder add(final String key, final TreeBuilder builder) {
			node.data.put(key, new DataNode.Value((DataNode) node.type.middle().get(key), builder.build()));
			return this;
		}

		public TreeBuilder add(final String key, final String text) {
			node.data.put(key, new DataPrimitive.Value((DataPrimitive) node.type.middle().get(key), text));
			return this;
		}

		public Node build() {
			return node;
		}

		public DataElement.Value buildArray() {
			return new DataArray.Value(null, Arrays.asList(node));
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
}
