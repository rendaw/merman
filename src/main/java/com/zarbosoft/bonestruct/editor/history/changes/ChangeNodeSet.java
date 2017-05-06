package com.zarbosoft.bonestruct.editor.history.changes;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.history.Change;

public class ChangeNodeSet extends Change {
	private final ValueNode value;
	private Node node;

	public ChangeNodeSet(final ValueNode value, final Node newValue) {
		this.value = value;
		node = newValue;
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
		node = other2.node;
		return true;
	}

	public Change apply(final Context context) {
		final Change reverse = new ChangeNodeSet(value, value.data);
		value.data.setParent(null);
		value.data = node;
		node.setParent(value.new NodeParent());
		for (final ValueNode.Listener listener : value.listeners)
			listener.set(context, node);
		return reverse;
	}
}
