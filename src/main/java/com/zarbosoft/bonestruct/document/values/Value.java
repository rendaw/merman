package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.DocumentNode;
import com.zarbosoft.bonestruct.document.DocumentNodeParent;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.syntax.back.BackDataKey;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePart;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

public abstract class Value extends DocumentNode {
	public Atom.Parent parent = null;

	public void setParent(final Atom.Parent parent) {
		this.parent = parent;
	}

	@Override
	public DocumentNodeParent parent() {
		return parent;
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
			final ValueArray.ArrayParent arrayParent = (ValueArray.ArrayParent) atom.parent;
			return parentPath.add(String.valueOf(arrayParent.actualIndex + subpath.first)).add(subpath.second);
		} else if (atom.parent.value().middle() instanceof MiddleRecord) {
			final String key = ((ValuePrimitive) atom.data.get(((BackDataKey) atom.type.back().get(0)).middle)).get();
			return parentPath.add(key).add(subpath.second);
		} else {
			return parentPath.add(subpath.second);
		}
	}

	public abstract boolean selectDown(Context context);

	public abstract class Parent extends DocumentNodeParent {

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
	}
}
