package com.zarbosoft.bonestruct.document.values;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.iterable;
import static java.util.Collections.unmodifiableList;

public class ValueArray extends Value {
	private final MiddleArrayBase middle;
	public final List<Node> data = new ArrayList<>();
	public final Set<Listener> listeners = new HashSet<>();

	public MiddleElement middle() {
		return middle;
	}

	public interface Listener {

		void changed(Context context, int index, int remove, List<Node> add);
	}

	public class ArrayParent extends Parent {
		public int index = 0;
		public int actualIndex = 0;

		@Override
		public void replace(final Context context, final Node node) {
			final int index = this.index;
			context.history.apply(context, new ChangeArray(ValueArray.this, index, 1, ImmutableList.of(node)));
		}

		@Override
		public void delete(final Context context) {
			context.history.apply(context, new ChangeArray(ValueArray.this, index, 1, ImmutableList.of()));
		}

		@Override
		public String childType() {
			return middle.type;
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
		int sum;
		if (from == 0) {
			sum = 0;
		} else {
			final Node prior = data.get(from - 1);
			final ArrayParent parent = (ArrayParent) prior.parent;
			sum = parent.actualIndex + prior.type.back().size();
		}
		for (final Pair<Integer, Node> p : iterable(enumerate(data.stream().skip(from), from))) {
			final ArrayParent parent = ((ArrayParent) p.second.parent);
			parent.index = p.first;
			parent.actualIndex = sum;
			sum += p.second.type.back().size();
		}
	}

	public ValueArray(final MiddleArrayBase middle) {
		this.middle = middle;

	}

	public List<Node> get() {
		return unmodifiableList(data);
	}
}
