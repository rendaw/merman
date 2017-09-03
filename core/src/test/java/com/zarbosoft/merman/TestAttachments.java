package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Editor;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.helper.*;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.rendaw.common.Common;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class TestAttachments {
	@Parameterized.Parameters
	public static Iterable<Object[]> parameters() {
		return ImmutableList.of(new Object[] {false}, new Object[] {true});
	}

	final public static FreeAtomType text;
	final public static Syntax syntax;

	static {
		text = new TypeBuilder("text")
				.middlePrimitive("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		syntax = new SyntaxBuilder("any").type(text).group("any", new GroupBuilder().type(text).build()).build();
	}

	public TestAttachments(final boolean flipSetOrder) {
		Editor.createSet = () -> new Set() {
			List<Object> inner = new ArrayList<>();

			@Override
			public int size() {
				return inner.size();
			}

			@Override
			public boolean isEmpty() {
				return inner.isEmpty();
			}

			@Override
			public boolean contains(final Object o) {
				return inner.contains(o);
			}

			@Override
			public Iterator iterator() {
				return inner.iterator();
			}

			@Override
			public Object[] toArray() {
				return inner.toArray();
			}

			@Override
			public Object[] toArray(final Object[] a) {
				return inner.toArray(a);
			}

			@Override
			public boolean add(final Object o) {
				if (contains(o))
					return false;
				if (flipSetOrder)
					inner.add(0, o);
				else
					inner.add(o);
				return true;
			}

			@Override
			public boolean remove(final Object o) {
				if (!contains(o))
					return false;
				inner.remove(o);
				return true;
			}

			@Override
			public boolean containsAll(final Collection c) {
				return inner.containsAll(c);
			}

			@Override
			public boolean addAll(final Collection c) {
				boolean out = false;
				for (final Object o : c)
					out = out || add(o);
				return out;
			}

			@Override
			public boolean retainAll(final Collection c) {
				boolean out = false;
				for (final Object o : ImmutableList.copyOf(c)) {
					if (!c.contains(o)) {
						out = true;
						remove(o);
					}
				}
				return out;
			}

			@Override
			public boolean removeAll(final Collection c) {
				boolean out = false;
				for (final Object o : c) {
					if (contains(o)) {
						out = true;
						remove(o);
					}
				}
				return out;
			}

			@Override
			public void clear() {
				inner.clear();
			}
		};
	}

	@Test
	public void testPrimitiveRemoveAttachments() {
		final Atom textAtom = new TreeBuilder(text).add("value", "hi\ndog").build();
		final ValuePrimitive value = (ValuePrimitive) textAtom.data.get("value");
		final Common.Mutable<Brick> lastBrick = new Common.Mutable<>(null);
		final Attachment listener = new Attachment() {
			@Override
			public void destroy(final Context context) {
				lastBrick.value = textAtom.visual.getLastBrick(context);
				lastBrick.value.addAttachment(context, this);
			}
		};
		new GeneralTestWizard(syntax, textAtom).run(context -> {
			textAtom.visual.getLastBrick(context).addAttachment(context, listener);
		}).run(context -> context.history.apply(context, new ChangePrimitiveRemove(value, 2, 1))).run(context -> {
			assertThat(lastBrick.value, equalTo(value.visual.lines.get(0).brick));
		});
	}

	@Test
	public void testPrimitiveExpandAttachments() {
		final Atom textAtom = new TreeBuilder(text).add("value", "higgs dogoid").build();
		final ValuePrimitive value = (ValuePrimitive) textAtom.data.get("value");
		final Common.Mutable<Brick> lastBrick = new Common.Mutable<>(null);
		final Attachment listener = new Attachment() {
			@Override
			public void destroy(final Context context) {
				lastBrick.value = textAtom.visual.getLastBrick(context);
				lastBrick.value.addAttachment(context, this);
			}
		};
		new GeneralTestWizard(syntax, textAtom)
				.resize(60)
				.checkCourseCount(2)
				.run(context -> {
					textAtom.visual.getLastBrick(context).addAttachment(context, listener);
				})
				.resize(100000)
				.checkCourseCount(1)
				.run(context -> assertThat(lastBrick.value, equalTo(value.visual.lines.get(0).brick)));
	}
}
