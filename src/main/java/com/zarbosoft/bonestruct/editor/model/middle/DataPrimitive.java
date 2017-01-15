package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.editor.changes.Change;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.visual.Context;
import javafx.beans.property.SimpleStringProperty;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "primitive")
public class DataPrimitive extends DataElement {
	public abstract static class Listener {
		public abstract void set(Context context, String value);

		public abstract void added(Context context, int index, String value);

		public abstract void removed(Context context, int index, int count);
	}

	public static class Value {
		private StringBuilder value = new StringBuilder();
		private final Set<Listener> listeners = new HashSet<>();

		public Value(final String data) {
			value = new StringBuilder(data);
		}

		public Value() {

		}

		public void addListener(final Listener listener) {
			listeners.add(listener);
		}

		public void removeListener(final Listener listener) {
			listeners.remove(listener);
		}

		public String get() {
			return value.toString();
		}

		public int length() {
			return value.length();
		}
	}

	public Value get(final Map<String, Object> data) {
		return (Value) data.get(id);
	}

	@Override
	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {

	}

	@Override
	public Object create() {
		return new SimpleStringProperty();
	}

	public static class ChangeSet extends Change {
		private final Value data;
		private String value;

		public ChangeSet(final Value data, final String value) {
			this.data = data;
			this.value = value;
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

		@Override
		public Change apply(final Context context) {
			final Change reverse = new ChangeSet(data, data.value.toString());
			data.value = new StringBuilder(value);
			for (final Listener listener : data.listeners)
				listener.set(context, value);
			return reverse;
		}
	}

	public static class ChangeAdd extends Change {
		private final Value data;
		private final int index;
		private final StringBuilder value;

		public ChangeAdd(final Value data, final int index, final String value) {
			this.data = data;
			this.index = index;
			this.value = new StringBuilder(value);
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
			if (other2.index > index + value.length())
				return false;
			value.insert(other2.index - index, other2.value);
			return true;
		}

		@Override
		public Change apply(final Context context) {
			data.value.insert(index, value);
			for (final Listener listener : data.listeners)
				listener.added(context, index, value.toString());
			return new ChangeRemove(data, index, value.length());
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
			final ChangeAdd reverse = new ChangeAdd(data, index, data.value.substring(index, index + size));
			data.value.delete(index, index + size);
			return reverse;
		}
	}
}
