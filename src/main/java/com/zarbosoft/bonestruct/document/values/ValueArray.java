package com.zarbosoft.bonestruct.document.values;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.history.changes.ChangeArrayAdd;
import com.zarbosoft.bonestruct.history.changes.ChangeArrayRemove;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.rendaw.common.Common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static java.util.Collections.unmodifiableList;

public class ValueArray extends Value {
	private final MiddleArrayBase middle;
	public final List<Node> data = new ArrayList<>();
	public final Set<Listener> listeners = new HashSet<>();

	public MiddleElement data() {
		return middle;
	}

	public static abstract class Listener {
		public abstract void added(Context context, int index, List<Node> nodes);

		public abstract void removed(Context context, int index, int count);
	}

	public class ArrayParent extends Node.Parent {
		public int index = 0;
		public int actualIndex = 0;

		@Override
		public void replace(final Context context, final Node node) {
			final int index = this.index;
			context.history.apply(context, new ChangeArrayRemove(ValueArray.this, index, 1));
			context.history.apply(context, new ChangeArrayAdd(ValueArray.this, index, ImmutableList.of(node)));
		}

		@Override
		public void delete(final Context context) {
			context.history.apply(context, new ChangeArrayRemove(ValueArray.this, index, 1));
		}

		@Override
		public String childType() {
			return middle.type;
		}

		@Override
		public Value value() {
			return ValueArray.this;
		}

		@Override
		public String id() {
			return middle.id;
		}

		@Override
		public Path getPath() {
			return middle.getPath(ValueArray.this, actualIndex);
		}
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public ValueArray(final MiddleArrayBase middle, final List<Node> data) {
		this.middle = middle;
		this.data.addAll(data);
		data.stream().forEach(v -> {
			v.setParent(new ArrayParent());
		});
		renumber(0);
	}

	public void renumber(final int from) {
		final Common.Mutable<Integer> sum = new Common.Mutable<>(0);
		enumerate(data.stream().skip(from), from).forEach(p -> {
			final ArrayParent parent = ((ArrayParent) p.second.parent);
			parent.index = p.first;
			parent.actualIndex = sum.value;
			sum.value += p.second.type.back().size();
		});
	}

	public ValueArray(final MiddleArrayBase middle) {
		this.middle = middle;

	}

	public List<Node> get() {
		return unmodifiableList(data);
	}
}
