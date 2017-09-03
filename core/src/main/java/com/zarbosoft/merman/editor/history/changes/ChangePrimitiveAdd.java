package com.zarbosoft.merman.editor.history.changes;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.Change;

public class ChangePrimitiveAdd extends Change {
	private final ValuePrimitive data;
	private final int index;
	private final StringBuilder value;

	public ChangePrimitiveAdd(final ValuePrimitive data, final int index, final String value) {
		this.data = data;
		this.index = index;
		this.value = new StringBuilder(value);
	}

	@Override
	public boolean merge(final Change other) {
		final ChangePrimitiveAdd other2;
		try {
			other2 = (ChangePrimitiveAdd) other;
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
		data.data.insert(index, value);
		for (final ValuePrimitive.Listener listener : data.listeners)
			listener.added(context, index, value.toString());
		return new ChangePrimitiveRemove(data, index, value.length());
	}
}
