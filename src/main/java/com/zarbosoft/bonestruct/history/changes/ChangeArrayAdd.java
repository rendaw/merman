package com.zarbosoft.bonestruct.history.changes;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.history.Change;

import java.util.ArrayList;
import java.util.List;

public class ChangeArrayAdd extends Change {
	private final ValueArray data;
	private final int index;
	private final List<Node> value = new ArrayList<>();

	public ChangeArrayAdd(final ValueArray data, final int index, final List<Node> value) {
		this.data = data;
		this.index = index;
		this.value.addAll(value);
	}

	@Override
	public boolean merge(final Change other) {
		final ChangeArrayAdd other2;
		try {
			other2 = (ChangeArrayAdd) other;
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
		data.data.addAll(index, value);
		value.stream().forEach(v -> {
			v.setParent(data.new ArrayParent());
		});
		data.renumber(index);
		for (final ValueArray.Listener listener : data.listeners)
			listener.added(context, index, value);
		return new ChangeArrayRemove(data, index, value.size());
	}

	@Override
	public Value getValue() {
		return data;
	}
}
