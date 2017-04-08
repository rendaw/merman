package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.bonestruct.syntax.middle.MiddleNode;

import java.util.HashSet;
import java.util.Set;

public class ValueNode extends Value {
	private final MiddleNode data;
	public Node value = null;
	public final Set<Listener> listeners = new HashSet<>();

	public abstract static class Listener {
		public abstract void set(Context context, Node node);
	}

	public class NodeParent extends Node.Parent {
		@Override
		public void replace(final Context context, final Node node) {
			context.history.apply(context, new ChangeNodeSet(ValueNode.this, node));
		}

		@Override
		public String childType() {
			return data.type;
		}

		@Override
		public Value data() {
			return ValueNode.this;
		}

		@Override
		public String id() {
			return data.id;
		}

		@Override
		public Path getPath() {
			return ValueNode.this.getPath();
		}
	}

	public ValueNode(final MiddleNode data, final Node value) {
		this.data = data;
		this.value = value;
		if (value != null)
			value.setParent(new NodeParent());
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public Node get() {
		return value;
	}

	@Override
	public MiddleElement data() {
		return data;
	}
}
