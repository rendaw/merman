package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.back.BackDataKey;
import com.zarbosoft.merman.syntax.middle.MiddleArray;
import com.zarbosoft.merman.syntax.middle.MiddlePart;
import com.zarbosoft.merman.syntax.middle.MiddleRecord;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

public abstract class Value {
	public Atom.Parent parent = null;

	public void setParent(final Atom.Parent parent) {
		this.parent = parent;
	}

	public abstract MiddlePart middle();

	final public Path getPath() {
		final Atom atom = parent.atom();
		if (atom.parent == null)
			return new Path();
		final Pair<Integer, Path> subpath = atom.type.getBackPart(middle().id).getSubpath();
		final Value parentValue = atom.parent.value();
		final Path parentPath = parentValue.getPath();
		if (atom.parent.value().middle() instanceof MiddleArray) {
			final com.zarbosoft.merman.document.values.ValueArray.ArrayParent arrayParent =
					(com.zarbosoft.merman.document.values.ValueArray.ArrayParent) atom.parent;
			return parentPath.add(String.valueOf(arrayParent.actualIndex + subpath.first)).add(subpath.second);
		} else if (atom.parent.value().middle() instanceof MiddleRecord) {
			final String key = (
					(com.zarbosoft.merman.document.values.ValuePrimitive) atom.data.get((
							(BackDataKey) atom.type.back().get(0)
					).middle)
			).get();
			return parentPath.add(key).add(subpath.second);
		} else {
			return parentPath.add(subpath.second);
		}
	}

	public abstract boolean selectDown(Context context);

	public abstract class Parent {

		/**
		 * Replace the child with a new atom.  (Creates history)
		 *
		 * @param context
		 * @param atom
		 */
		public abstract void replace(Context context, Atom atom);

		/**
		 * Remove the element if an array.  (Creates history)
		 *
		 * @param context
		 */
		public void delete(final Context context) {
			throw new DeadCode();
		}

		public abstract String childType();

		public Value value() {
			return Value.this;
		}

		public String id() {
			return middle().id;
		}

		public abstract Path path();

		public abstract boolean selectUp(final Context context);
	}
}
