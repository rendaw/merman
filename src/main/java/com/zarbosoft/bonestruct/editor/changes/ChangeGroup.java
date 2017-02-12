package com.zarbosoft.bonestruct.editor.changes;

import com.google.common.collect.Lists;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.pidgoon.internal.Helper;

import java.util.ArrayList;
import java.util.List;

public class ChangeGroup {
	private final List<Change> subchanges = new ArrayList<>();

	public void add(final Change change) {
		if (subchanges.isEmpty() || !Helper.last(subchanges).merge(change))
			subchanges.add(change);
	}

	public ChangeGroup apply(final Context context) {
		final ChangeGroup out = new ChangeGroup();
		for (final Change change : Lists.reverse(subchanges))
			out.subchanges.add(change.apply(context));
		return out;
	}

	public boolean isEmpty() {
		return subchanges.isEmpty();
	}
}
