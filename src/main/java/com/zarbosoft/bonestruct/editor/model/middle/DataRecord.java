package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.editor.InvalidDocument;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.changes.Change;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.pidgoon.internal.Pair;

import java.util.*;

@Luxem.Configuration(name = "record")
public class DataRecord extends DataElement {
	public abstract static class Listener {
		public abstract void added(Context context, String key, DataNode.Value value);

		public abstract void removed(Context context, String key);
	}

	public static class Value {
		private final Map<String, DataNode.Value> value = new HashMap<>();
		private final Set<Listener> listeners = new HashSet<>();

		public Value(final List<Pair<String, DataNode.Value>> data) {
			for (final Pair<String, DataNode.Value> pair : data) {
				if (value.containsKey(pair.first))
					throw new InvalidDocument("Duplicate key in record.");
				value.put(pair.first, pair.second);
			}
		}

		public Value() {

		}

		public void addListener(final Listener listener) {
			listeners.add(listener);
		}

		public void removeListener(final Listener listener) {
			listeners.remove(listener);
		}

		public Map<String, DataNode.Value> get() {
			return Collections.unmodifiableMap(value);
		}
	}

	public static class ChangeAdd extends Change {
		private final Value data;
		private final String key;
		private final DataNode.Value value;

		public ChangeAdd(final Value data, final String key, final DataNode.Value value) {
			this.data = data;
			this.key = key;
			this.value = value;
		}

		@Override
		public boolean merge(final Change other) {
			return false;
		}

		@Override
		public Change apply(final Context context) {
			final ChangeRemove out = new ChangeRemove(data, key);
			data.value.put(key, value);
			for (final Listener listener : data.listeners)
				listener.added(context, key, value);
			return out;
		}
	}

	public static class ChangeRemove extends Change {

		private final Value data;
		private final String key;

		public ChangeRemove(final Value data, final String key) {
			this.data = data;
			this.key = key;
		}

		@Override
		public boolean merge(final Change other) {
			return false;
		}

		@Override
		public Change apply(final Context context) {
			final ChangeAdd out = new ChangeAdd(data, key, data.value.get(key));
			for (final Listener listener : data.listeners)
				listener.removed(context, key);
			data.value.remove(key);
			return null;
		}
	}

	public static class ChangeReplace extends Change {
		private final Value data;
		private final String key;
		private String newKey;

		public ChangeReplace(final Value data, final String key, final String newKey) {
			this.data = data;
			this.key = key;
			this.newKey = newKey;
		}

		@Override
		public boolean merge(final Change other) {
			final ChangeReplace other2;
			try {
				other2 = (ChangeReplace) other;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other2.data != data)
				return false;
			if (!other2.key.equals(newKey))
				return false;
			newKey = other2.newKey;
			return true;
		}

		@Override
		public Change apply(final Context context) {
			final ChangeReplace out = new ChangeReplace(data, newKey, key);
			for (final Listener listener : data.listeners)
				listener.removed(context, key);
			final DataNode.Value value = data.value.remove(key);
			data.value.put(key, value);
			for (final Listener listener : data.listeners)
				listener.added(context, key, value);
			return out;
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
