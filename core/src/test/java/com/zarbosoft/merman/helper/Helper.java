package com.zarbosoft.merman.helper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.ClipboardEngine;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.editor.history.History;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.back.*;
import com.zarbosoft.merman.syntax.middle.MiddleArrayBase;
import com.zarbosoft.rendaw.common.DeadCode;
import org.junit.ComparisonFailure;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

import static com.zarbosoft.rendaw.common.Common.*;

public class Helper {
	public static void dump(final Value value, final RawWriter writer) {
		uncheck(() -> {
			if (value.getClass() == ValueArray.class) {
				writer.arrayBegin();
				((ValueArray) value).data.stream().forEach(element -> dump(element, writer));
				writer.arrayEnd();
			} else if (value.getClass() == ValueAtom.class) {
				dump(((ValueAtom) value).get(), writer);
			} else if (value.getClass() == ValuePrimitive.class) {
				writer.quotedPrimitive(((ValuePrimitive) value).get().getBytes(StandardCharsets.UTF_8));
			} else
				throw new DeadCode();
		});
	}

	private static void dump(final Atom value, final RawWriter writer) {
		uncheck(() -> {
			writer.type(value.type.id().getBytes(StandardCharsets.UTF_8));
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

	public static void act(final Context context, final String name) {
		for (final Action action : iterable(context.actions())) {
			if (action.id().equals(name)) {
				action.run(context);
				return;
			}
		}
		throw new AssertionError(String.format("No action named [%s]", name));
	}

	public static BackPart buildBackType(final String type, final BackPart child) {
		final BackType back = new BackType();
		back.type = type;
		back.value = child;
		return back;
	}

	public static BackPart buildBackPrimitive(final String value) {
		final BackPrimitive back = new BackPrimitive();
		back.value = value;
		return back;
	}

	public static BackPart buildBackDataAtom(final String middle) {
		final BackDataAtom back = new BackDataAtom();
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

	public static BackPart buildBackDataRootArray(final String middle) {
		final BackDataRootArray back = new BackDataRootArray();
		back.middle = middle;
		return back;
	}

	public static void assertTreeEqual(final Atom expected, final Atom got) {
		if (expected.type != got.type)
			throw new AssertionError(String.format("Atom type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
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
			if (expectedValue.data.size() != gotValue.data.size())
				throw new AssertionError(String.format("Array length mismatch.\nExpected: %s\nGot: %s\nAt: %s",
						expectedValue.data.size(),
						gotValue.data.size(),
						got.getPath()
				));
			zip(expectedValue.data.stream(), gotValue.data.stream()).forEach(pair -> assertTreeEqual(pair.first,
					pair.second
			));
		} else if (expected.getClass() == ValueAtom.class) {
			final ValueAtom expectedValue = (ValueAtom) expected;
			final ValueAtom gotValue = (ValueAtom) got;
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
			throw new AssertionError(String.format("Atom type mismatch.\nExpected: %s\nGot: %s\nAt: %s",
					expected.getClass(),
					got.getClass(),
					got.getPath()
			));
	}

	public static void assertTreeEqual(
			final Context context, final Atom expected, final Value got
	) {
		assertTreeEqual(new ValueArray((MiddleArrayBase) context.syntax.root.middle.get("value"),
				ImmutableList.of(expected)
		), got);
	}

	public static ValueArray rootArray(final Document doc) {
		return (ValueArray) doc.root.data.get("value");
	}

	public static Context buildDoc(final Syntax syntax, final Atom... root) {
		return buildDoc(idleTask -> {
		}, limit -> {
		}, syntax, root);
	}

	public static Context buildDoc(
			final Consumer<IterationTask> addIteration,
			final Consumer<Integer> flushIteration,
			final Syntax syntax,
			final Atom... root
	) {
		final Document doc = new Document(syntax,
				new Atom(syntax.root,
						ImmutableMap.of("value",
								new ValueArray((MiddleArrayBase) syntax.root.middle.get("value"), Arrays.asList(root))
						)
				)
		);
		final Context context = new Context(syntax,
				doc,
				new MockeryDisplay(),
				addIteration,
				flushIteration,
				new History(),
				new ClipboardEngine() {
					byte[] data = null;
					String string = null;

					@Override
					public void set(final byte[] bytes) {
						data = bytes;
					}

					@Override
					public void setString(final String string) {
						this.string = string;
					}

					@Override
					public byte[] get() {
						return data;
					}

					@Override
					public String getString() {
						return string;
					}
				}
		);
		return context;
	}
}
