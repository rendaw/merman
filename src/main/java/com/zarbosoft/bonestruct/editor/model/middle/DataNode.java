package com.zarbosoft.bonestruct.editor.model.middle;

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

	public static class Value {
		private Node value = null;
		private final Set<Listener> listeners = new HashSet<>();

		public Value(final Node data) {
			value = data;
		}

		public Value() {

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
	}

	public static class ChangeSet extends Change {
		private final Value data;
		private Node value;

		public ChangeSet(final Value data, final Node newValue) {
			this.data = data;
			value = newValue;
		}

		@Override
		public boolean merge(final Change other) {
			final ChangeSet other2;
			try {
				other2 = (ChangeSet) other;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other2.data != data)
				return false;
			value = other2.value;
			return true;
		}

		public Change apply(final Context context) {
			final Change reverse = new ChangeSet(data, data.value);
			data.value = value;
			for (final Listener listener : data.listeners)
				listener.set(context, value);
			return reverse;
		}
	}

	@Luxem.Configuration
	public String tag;

	public Value get(final Map<String, Object> data) {
		return (Value) data.get(id);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		if (!singleNodes.contains(tag))
			throw new InvalidSyntax(String.format("Unknown unit node or tag id [%s].", tag));
	}

	@Override
	public Object create() {
		return new Value();
	}
}
