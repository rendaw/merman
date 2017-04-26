package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.bonestruct.syntax.middle.MiddleNode;

import java.util.HashSet;
import java.util.Set;

public class ValueNode extends Value {
	private final MiddleNode middle;
	public Node data = null; // INVARIANT: Never null when in tree
	public final Set<Listener> listeners = new HashSet<>();

	public abstract static class Listener {
		public abstract void set(Context context, Node node);
	}

	public class NodeParent extends Parent {
		@Override
		public void replace(final Context context, final Node node) {
			context.history.apply(context, new ChangeNodeSet(ValueNode.this, node));
		}

		@Override
		public void delete(final Context context) {
			context.history.apply(context, new ChangeNodeSet(ValueNode.this, context.syntax.gap.create()));
		}

		@Override
		public String childType() {
			return middle.type;
		}

		@Override
		public Value value() {
			return ValueNode.this;
		}

		@Override
		public String id() {
			return middle.id;
		}

		@Override
		public Path getPath() {
			return ValueNode.this.getPath();
		}
	}

	public ValueNode(final MiddleNode middle, final Node data) {
		this.middle = middle;
		this.data = data;
		if (data != null)
			data.setParent(new NodeParent());
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public Node get() {
		return data;
	}

	@Override
	public MiddleElement middle() {
		return middle;
	}
}
