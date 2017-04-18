package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.Map;
import java.util.Set;

public class Node {
	public Parent parent;
	public NodeType type;
	private final Map<String, Value> data;
	private NodeType.NodeTypeVisual visual;

	public Node(final NodeType type, final Map<String, Value> data) {
		this.type = type;
		this.data = data;
		data.forEach((k, v) -> {
			v.setParent(new Value.Parent() {
				@Override
				public Node node() {
					return Node.this;
				}
			});
		});
	}

	public Value data(final String key) {
		return data.get(key);
	}

	public Set<String> dataKeys() {
		return data.keySet();
	}

	public Path getPath() {
		if (parent == null)
			return new Path();
		else
			return parent.getPath();
	}

	public abstract static class Parent {

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

		public abstract Value value();

		public abstract String id();

		public abstract Path getPath();

	}

	public Visual createVisual(final Context context) {
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
