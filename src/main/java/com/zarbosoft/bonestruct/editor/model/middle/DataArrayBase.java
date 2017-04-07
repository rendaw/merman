package com.zarbosoft.bonestruct.editor.model.middle;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.changes.Change;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Common;

import java.util.*;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static java.util.Collections.unmodifiableList;

@Configuration
public abstract class DataArrayBase extends DataElement {
	public static abstract class Listener {
		public abstract void added(Context context, int index, List<Node> nodes);

		public abstract void removed(Context context, int index, int count);
	}

	public static class Value extends DataElement.Value {
		private final DataArrayBase data;
		protected final List<Node> value = new ArrayList<>();
		private final Set<Listener> listeners = new HashSet<>();

		public DataElement data() {
			return data;
		}

		public class ArrayParent extends Node.Parent {
			public int index = 0;
			public int actualIndex = 0;

			@Override
			public void replace(final Context context, final Node node) {
				final int index = this.index;
				context.history.apply(context, new ChangeRemove(Value.this, index, 1));
				context.history.apply(context, new ChangeAdd(Value.this, index, ImmutableList.of(node)));
			}

			@Override
			public void delete(final Context context) {
				context.history.apply(context, new ChangeRemove(Value.this, index, 1));
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
				return data.getPath(Value.this, actualIndex);
			}
		}

		public void addListener(final Listener listener) {
			listeners.add(listener);
		}

		public void removeListener(final Listener listener) {
			listeners.remove(listener);
		}

		public Value(final DataArrayBase data, final List<Node> value) {
			this.data = data;
			this.value.addAll(value);
			value.stream().forEach(v -> {
				v.setParent(new ArrayParent());
			});
			renumber(0);
		}

		private void renumber(final int from) {
			final Common.Mutable<Integer> sum = new Common.Mutable<>(0);
			enumerate(value.stream().skip(from), from).forEach(p -> {
				final ArrayParent parent = ((ArrayParent) p.second.parent);
				parent.index = p.first;
				parent.actualIndex = sum.value;
				sum.value += p.second.type.back().size();
			});
		}

		public Value(final DataArrayBase data) {
			this.data = data;

		}

		public List<Node> get() {
			return unmodifiableList(value);
		}
	}

	public abstract Path getPath(final Value value, final int actualIndex);

	public static class ChangeAdd extends Change {
		private final Value data;
		private final int index;
		private final List<Node> value = new ArrayList<>();

		public ChangeAdd(final Value data, final int index, final List<Node> value) {
			this.data = data;
			this.index = index;
			this.value.addAll(value);
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
			value.stream().forEach(v -> {
				v.setParent(data.new ArrayParent());
			});
			data.renumber(index);
			for (final Listener listener : data.listeners)
				listener.added(context, index, value);
			return new ChangeRemove(data, index, value.size());
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
			final ChangeAdd reverse =
					new ChangeAdd(data, index, ImmutableList.copyOf(data.value.subList(index, index + size)));
			data.value.subList(index, index + size).clear();
			data.renumber(index);
			for (final Listener listener : data.listeners)
				listener.removed(context, index, size);
			return reverse;
		}

		@Override
		public DataElement.Value getValue() {
			return data;
		}
	}

	@Configuration
	public String type;

	@Override
	public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {
		if (type != null && !allTypes.contains(type))
			throw new InvalidSyntax(String.format("Unknown type [%s].", type));
	}

	@Override
	public DataElement.Value create(final Syntax syntax) {
		return new Value(this);
	}

	public Value get(final Map<String, DataElement.Value> data) {
		return (Value) data.get(id);
	}
}
