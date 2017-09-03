package com.zarbosoft.merman.editor.history.changes;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.Change;

public class ChangeNodeSet extends Change {
	private final ValueAtom value;
	private Atom atom;

	public ChangeNodeSet(final ValueAtom value, final Atom newValue) {
		this.value = value;
		atom = newValue;
	}

	@Override
	public boolean merge(final Change other) {
		final ChangeNodeSet other2;
		try {
			other2 = (ChangeNodeSet) other;
		} catch (final ClassCastException e) {
			return false;
		}
		if (other2.value != value)
			return false;
		atom = other2.atom;
		return true;
	}

	public Change apply(final Context context) {
		final Change reverse = new ChangeNodeSet(value, value.data);
		value.data.setParent(null);
		value.data = atom;
		atom.setParent(value.new NodeParent());
		for (final ValueAtom.Listener listener : value.listeners)
			listener.set(context, atom);
		return reverse;
	}
}
