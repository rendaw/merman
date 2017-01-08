package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.Map;

public class ConcensusAlignment extends Alignment {
	@Override
	public void set(final Context context, final int gotConverse) {
		if (gotConverse == this.converse)
			return;
		if (gotConverse > this.converse) {
			this.converse = gotConverse;
		} else {
			reduce(context);
		}
		submit(context);
	}

	@Override
	public void removeListener(
			final Context context, final AlignmentListener listener
	) {
		super.removeListener(context, listener);
		if (listener.getMinConverse(context) == converse) {
			reduce(context);
			submit(context);
		}
	}

	private void reduce(final Context context) {
		converse = 0;
		for (final AlignmentListener listener : listeners) {
			converse = Math.max(listener.getMinConverse(context), converse);
		}
	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
	}
}
