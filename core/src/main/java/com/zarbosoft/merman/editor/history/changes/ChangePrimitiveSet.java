package com.zarbosoft.merman.editor.history.changes;

import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.Change;

public class ChangePrimitiveSet extends Change {
	private final ValuePrimitive data;
	private String value;

	public ChangePrimitiveSet(final ValuePrimitive data, final String value) {
		this.data = data;
		this.value = value;
	}

	@Override
	public boolean merge(final Change other) {
		final ChangePrimitiveSet other2;
		try {
			other2 = (ChangePrimitiveSet) other;
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
		final Change reverse = new ChangePrimitiveSet(data, data.data.toString());
		data.data = new StringBuilder(value);
		for (final ValuePrimitive.Listener listener : data.listeners)
			listener.set(context, value);
		return reverse;
	}
}
