package com.zarbosoft.bonestruct.editor.history.changes;

import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.history.Change;

public class ChangePrimitiveRemove extends Change {

	private final ValuePrimitive data;
	private int index;
	private int size;

	public ChangePrimitiveRemove(final ValuePrimitive data, final int index, final int size) {
		this.data = data;
		this.index = index;
		this.size = size;
	}

	@Override
	public boolean merge(final Change other) {
		if (!(other instanceof ChangePrimitiveRemove))
			return false;
		final ChangePrimitiveRemove other2 = (ChangePrimitiveRemove) other;
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
		final ChangePrimitiveAdd reverse =
				new ChangePrimitiveAdd(data, index, data.data.substring(index, index + size));
		data.data.delete(index, index + size);
		for (final ValuePrimitive.Listener listener : data.listeners)
			listener.removed(context, index, size);
		return reverse;
	}
}
