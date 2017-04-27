package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.back.BackDataKey;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

public abstract class Value {
	public Node.Parent parent = null;
	public Visual visual = null;

	public void setParent(final Node.Parent parent) {
		this.parent = parent;
	}

	public abstract MiddleElement middle();

	final public Path getPath() {
		if (parent == null)
			return new Path();
		final Node node = parent.node();
		final Pair<Integer, Path> subpath = node.type.getBackPart(middle().id).getSubpath();
		final Value parentValue = node.parent.value();
		final Path parentPath = parentValue.getPath();
		if (node.parent.value().middle() instanceof MiddleArray) {
			final ValueArray.ArrayParent arrayParent = (ValueArray.ArrayParent) node.parent;
			return parentPath.add(String.valueOf(arrayParent.actualIndex + subpath.first)).add(subpath.second);
		} else if (node.parent.value().middle() instanceof MiddleRecord) {
			final String key = ((ValuePrimitive) node.data.get(((BackDataKey) node.type.back().get(0)).middle)).get();
			return parentPath.add(key).add(subpath.second);
		} else {
			return parentPath.add(subpath.second);
		}
	}

	public Visual getVisual() {
		return visual;
	}

	public abstract class Parent {

		/**
		 * Replace the child with a new node.  (Creates history)
		 *
		 * @param context
		 * @param node
		 */
		public abstract void replace(Context context, Node node);

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

		public abstract Path getPath();
	}
}
