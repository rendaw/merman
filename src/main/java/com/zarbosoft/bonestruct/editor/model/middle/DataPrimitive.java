package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.editor.changes.Change;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.pidgoon.Node;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.interface1.Configuration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration(name = "primitive")
public class DataPrimitive extends DataElement {

	@Configuration(optional = true, description = "An expression grammar describing valid primitive contents.")
	public Node validation;

	public abstract static class Listener {
		public abstract void set(Context context, String value);

		public abstract void added(Context context, int index, String value);

		public abstract void removed(Context context, int index, int count);
	}

	public static class Value extends DataElement.Value {
		public final DataPrimitive data;
		private StringBuilder value = new StringBuilder();
		private final Set<Listener> listeners = new HashSet<>();

		public Value(final DataPrimitive data, final String value) {
			this.data = data;
			this.value = new StringBuilder(value);
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

		@Override
		public DataElement data() {
			return data;
		}

		public Change changeRemove(final int begin, final int length) {
			return data.changeRemove(this, begin, length);
		}

		public Change changeAdd(final int begin, final String text) {
			return data.changeAdd(this, begin, text);
		}
	}

	protected Change changeAdd(final Value value, final int begin, final String text) {
		return new DataPrimitive.ChangeAdd(value, begin, text);
	}

	protected Change changeRemove(final Value value, final int begin, final int length) {
		return new ChangeRemove(value, begin, length);
	}

	public Value get(final Map<String, DataElement.Value> data) {
		return (Value) data.get(id);
	}

	@Override
	public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {

	}

	@Override
	public DataElement.Value create(final Syntax syntax) {
		return new Value(this, "");
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

		@Override
		public DataElement.Value getValue() {
			return data;
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

		@Override
		public DataElement.Value getValue() {
			return data;
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
			if (!(other instanceof ChangeRemove))
				return false;
			final ChangeRemove other2 = (ChangeRemove) other;
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
			final ChangeAdd reverse = new ChangeAdd(data, index, data.value.substring(index, index + size));
			data.value.delete(index, index + size);
			for (final Listener listener : data.listeners)
				listener.removed(context, index, size);
			return reverse;
		}

		@Override
		public DataElement.Value getValue() {
			return data;
		}
	}
}
