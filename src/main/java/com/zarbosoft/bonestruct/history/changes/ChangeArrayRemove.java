package com.zarbosoft.bonestruct.history.changes;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.history.Change;

public class ChangeArrayRemove extends Change {

	private final ValueArray data;
	private int index;
	private int size;

	public ChangeArrayRemove(final ValueArray data, final int index, final int size) {
		this.data = data;
		this.index = index;
		this.size = size;
	}

	@Override
	public boolean merge(final Change other) {
		if (!(other instanceof ChangeArrayRemove))
			return false;
		final ChangeArrayRemove other2 = (ChangeArrayRemove) other;
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
		final ChangeArrayAdd reverse =
				new ChangeArrayAdd(data, index, ImmutableList.copyOf(data.value.subList(index, index + size)));
		data.value.subList(index, index + size).clear();
		data.renumber(index);
		for (final ValueArray.Listener listener : data.listeners)
			listener.removed(context, index, size);
		return reverse;
	}

	@Override
	public Value getValue() {
		return data;
	}
}
