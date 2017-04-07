package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.back.BackDataKey;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

@Configuration
public abstract class DataElement {
	public String id;

	public abstract void finish(Set<String> allTypes, Set<String> scalarTypes);

	public abstract Value create(Syntax syntax);

	public static abstract class Parent {
		public abstract Node node();
	}

	public static abstract class Value {
		public Parent parent = null;
		public VisualNode visual = null;

		public void setParent(final Parent parent) {
			this.parent = parent;
		}

		public Parent parent() {
			return parent;
		}

		public abstract DataElement data();

		final public Path getPath() {
			if (parent == null)
				return new Path();
			final Node node = parent.node();
			final Pair<Integer, Path> subpath = node.type.getBackPart(data().id).getSubpath();
			final Value parentValue = node.parent.data();
			final Path parentPath = parentValue.getPath();
			if (node.parent.data().data() instanceof DataArray) {
				final DataArrayBase.Value.ArrayParent arrayParent = (DataArrayBase.Value.ArrayParent) node.parent;
				return parentPath.add(String.valueOf(arrayParent.actualIndex + subpath.first)).add(subpath.second);
			} else if (node.parent.data().data() instanceof DataRecord) {
				final String key =
						((DataRecordKey.Value) node.data(((BackDataKey) node.type.back().get(0)).middle)).get();
				return parentPath.add(key).add(subpath.second);
			} else {
				return parentPath.add(subpath.second);
			}
		}

		public VisualNode getVisual() {
			return visual;
		}
	}

	// TODO
	/*
	@Configuration(optional = true)
	boolean optional = false;
	 */
}
