package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.back.BackDataKey;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.rendaw.common.Pair;

public abstract class Value {
	public Parent parent = null;
	public Visual visual = null;

	public void setParent(final Parent parent) {
		this.parent = parent;
	}

	public Parent parent() {
		return parent;
	}

	public abstract MiddleElement data();

	final public Path getPath() {
		if (parent == null)
			return new Path();
		final Node node = parent.node();
		final Pair<Integer, Path> subpath = node.type.getBackPart(data().id).getSubpath();
		final Value parentValue = node.parent.value();
		final Path parentPath = parentValue.getPath();
		if (node.parent.value().data() instanceof MiddleArray) {
			final ValueArray.ArrayParent arrayParent = (ValueArray.ArrayParent) node.parent;
			return parentPath.add(String.valueOf(arrayParent.actualIndex + subpath.first)).add(subpath.second);
		} else if (node.parent.value().data() instanceof MiddleRecord) {
			final String key = ((ValuePrimitive) node.data(((BackDataKey) node.type.back().get(0)).middle)).get();
			return parentPath.add(key).add(subpath.second);
		} else {
			return parentPath.add(subpath.second);
		}
	}

	public Visual getVisual() {
		return visual;
	}

	public static abstract class Parent {
		public abstract Node node();
	}
}
