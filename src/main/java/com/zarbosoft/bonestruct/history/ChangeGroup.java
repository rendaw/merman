package com.zarbosoft.bonestruct.history;

import com.google.common.collect.Lists;
import com.zarbosoft.bonestruct.editor.Context;

import java.util.ArrayList;
import java.util.List;

import static com.zarbosoft.rendaw.common.Common.last;

public class ChangeGroup extends Change {
	public final List<Change> subchanges = new ArrayList<>();

	@Override
	public boolean merge(final Change other) {
		if (other instanceof ChangeGroup) {
			subchanges.addAll(((ChangeGroup) other).subchanges);
		} else if (subchanges.isEmpty()) {
			subchanges.add(other);
		} else if (last(subchanges).merge(other)) {
		} else
			subchanges.add(other);
		return true;
	}

	@Override
	public Change apply(final Context context) {
		final ChangeGroup out = new ChangeGroup();
		for (final Change change : Lists.reverse(subchanges)) {
			out.subchanges.add(change.apply(context));
		}
		return out;
	}

	public boolean isEmpty() {
		return subchanges.isEmpty();
	}
}
