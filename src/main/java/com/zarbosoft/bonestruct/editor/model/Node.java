package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;

import java.util.Map;

public class Node {
	public Parent parent;
	public NodeType type;
	public Map<String, DataElement.Value> data;
	private NodeType.NodeTypeVisual visual;

	public Node(final NodeType type, final Map<String, DataElement.Value> data) {
		this.type = type;
		this.data = data;
		data.forEach((k, v) -> {
			v.setParent(new DataElement.Parent() {
				@Override
				public Node node() {
					return Node.this;
				}
			});
		});
	}

	public abstract static class Parent {

		/**
		 * Replace the child with a new node.  (Creates history)
		 *
		 * @param context
		 * @param node
		 */
		public abstract void replace(Context context, Node node);

		public abstract String childType();

		public abstract DataElement.Value data();

		public abstract String id();

		public abstract Path getPath();
	}

	public VisualNode createVisual(final Context context) {
		this.visual = type.createVisual(context, data);
		return visual;
	}

	public void setParent(final Parent parent) {
		this.parent = parent;
	}

	public NodeType.NodeTypeVisual getVisual() {
		return visual;
	}
}
