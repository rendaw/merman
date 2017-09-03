package com.zarbosoft.merman.editor.history.changes;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.Change;

import java.util.ArrayList;
import java.util.List;

public class ChangeArray extends Change {

	private final ValueArray value;
	private int index;
	private int remove;
	private final List<Atom> add = new ArrayList<>();

	public ChangeArray(final ValueArray value, final int index, final int remove, final List<Atom> add) {
		this.value = value;
		this.index = index;
		this.remove = remove;
		this.add.addAll(add);
	}

	@Override
	public boolean merge(final Change other) {
		final ChangeArray other2;
		try {
			other2 = (ChangeArray) other;
		} catch (final ClassCastException e) {
			return false;
		}
		if (other2.value != value)
			return false;
		if (other2.index + other2.remove == index) {
			index = other2.index;
			remove += other2.remove;
			add.addAll(0, other2.add);
		} else if (index + remove == other2.index) {
			remove += other2.remove;
			add.addAll(other2.add);
		} else
			return false;
		return true;
	}

	@Override
	public Change apply(final Context context) {
		if (add.isEmpty() && index == 0 && remove == value.data.size() && value.parent.atom().parent == null) {
			add.add(context.syntax.gap.create());
		}
		final List<Atom> clearSublist = value.data.subList(index, index + remove);
		final ChangeArray reverse = new ChangeArray(value, index, add.size(), ImmutableList.copyOf(clearSublist));
		clearSublist.stream().forEach(v -> v.setParent(null));
		clearSublist.clear();
		value.data.addAll(index, add);
		add.stream().forEach(v -> {
			v.setParent(value.new ArrayParent());
		});
		value.renumber(index);
		for (final ValueArray.Listener listener : value.listeners) {
			listener.changed(context, index, remove, add);
		}
		return reverse;
	}
}
