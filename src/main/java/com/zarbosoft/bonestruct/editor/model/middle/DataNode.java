package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.changes.Change;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "node")
public class DataNode extends DataElement {
	public abstract static class Listener {
		public abstract void set(Context context, Node node);
	}

	public static class Value extends DataElement.Value {
		private final DataNode data;
		public Parent parent = null;
		private Node value = null;
		private final Set<Listener> listeners = new HashSet<>();

		private class NodeParent extends Node.Parent {

			@Override
			public void replace(final Context context, final Node node) {
				context.history.apply(context, new ChangeSet(Value.this, node));
			}

			@Override
			public String childType() {
				return data.type;
			}

			@Override
			public DataElement.Value data() {
				return Value.this;
			}

			@Override
			public String id() {
				return data.id;
			}

			@Override
			public Path getPath() {
				return Value.this.getPath();
			}
		}

		public Value(final DataNode data, final Node value) {
			this.data = data;
			this.value = value;
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

		public void setParent(final Parent parent) {
			this.parent = parent;
		}

		@Override
		public Parent parent() {
			return parent;
		}

		@Override
		public Path getPath() {
			return parent.node().type.getBackPart(data.id).getPath(parent.node().parent.getPath());
		}
	}

	public static class ChangeSet extends Change {
		private final Value value;
		private Node node;

		public ChangeSet(final Value value, final Node newValue) {
			this.value = value;
			node = newValue;
		}

		@Override
		public boolean merge(final Change other) {
			final ChangeSet other2;
			try {
				other2 = (ChangeSet) other;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other2.value != value)
				return false;
			node = other2.node;
			return true;
		}

		public Change apply(final Context context) {
			final Change reverse = new ChangeSet(value, value.value);
			value.value = node;
			node.setParent(value.new NodeParent());
			for (final Listener listener : value.listeners)
				listener.set(context, node);
			return reverse;
		}

		@Override
		public DataElement.Value getValue() {
			return value;
		}
	}

	@Luxem.Configuration
	public String type;

	public Value get(final Map<String, DataElement.Value> data) {
		return (Value) data.get(id);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		if (!singleNodes.contains(type))
			throw new InvalidSyntax(String.format("Unknown type [%s].", type));
	}

	@Override
	public DataElement.Value create() {
		return new Value(this, null);
	}
}
