package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.changes.Change;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.luxemj.Luxem;

import java.util.*;

import static java.util.Collections.unmodifiableList;

@Luxem.Configuration(name = "array")
public class DataArray extends DataElement {
	public static abstract class Listener {
		public abstract void added(Context context, int index, List<DataNode.Value> nodes);

		public abstract void removed(Context context, int index, int count);
	}

	public static class Value {
		private final List<DataNode.Value> value = new ArrayList<>();
		private final Set<Listener> listeners = new HashSet<>();

		public void addListener(final Listener listener) {
			listeners.add(listener);
		}

		public void removeListener(final Listener listener) {
			listeners.remove(listener);
		}

		public Value(final List<DataNode.Value> data) {
			value.addAll(data);
		}

		public Value() {

		}

		public List<DataNode.Value> get() {
			return unmodifiableList(value);
		}
	}

	public static class ChangeAdd extends Change {
		private final Value data;
		private final int index;
		private final List<DataNode.Value> value;

		public ChangeAdd(final Value data, final int index, final List<DataNode.Value> value) {
			this.data = data;
			this.index = index;
			this.value = value;
		}

		@Override
		public boolean merge(final Change other) {
			final ChangeAdd other2;
			try {
				other2 = (ChangeAdd) other;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other2.data != data)
				return false;
			if (other2.index < index)
				return false;
			if (other2.index > index + value.size())
				return false;
			value.addAll(other2.index - index, other2.value);
			return true;
		}

		@Override
		public Change apply(final Context context) {
			data.value.addAll(index, value);
			for (final Listener listener : data.listeners)
				listener.added(context, index, value);
			return new ChangeRemove(data, index, value.size());
		}
	}

	public static class ChangeRemove extends Change {

		private final Value data;
		private int index;
		private int size;

		public ChangeRemove(final Value data, final int index, final int size) {
			this.data = data;
			this.index = index;
			this.size = size;
		}

		@Override
		public boolean merge(final Change other) {
			final ChangeRemove other2;
			try {
				other2 = (ChangeRemove) other;
			} catch (final ClassCastException e) {
				return false;
			}
			if (other2.data != data)
				return false;
			if (other2.index + other2.size < index)
				return false;
			if (other2.index > index + size)
				return false;
			if (other2.index < other2.size) {
				index = other2.index;
				size += other2.size;
			} else {
				size += other2.size;
			}
			return true;
		}

		@Override
		public Change apply(final Context context) {
			for (final Listener listener : data.listeners)
				listener.removed(context, index, size);
			final ChangeAdd reverse = new ChangeAdd(data, index, data.value.subList(index, index + size));
			data.value.subList(index, index + size).clear();
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
		if (!arrayNodes.contains(tag))
			throw new InvalidSyntax(String.format("Unknown node or tag id [%s].", tag));
	}

	@Override
	public Object create() {
		return new Value();
	}
}
