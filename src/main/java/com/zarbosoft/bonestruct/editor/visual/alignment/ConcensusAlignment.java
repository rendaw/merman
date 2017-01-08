package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.Map;

public class ConcensusAlignment extends Alignment {
	@Override
	public void set(final Context context, final int gotConverse) {
		if (gotConverse > this.converse) {
			this.converse = gotConverse;
		} else {
			final int oldConverse = converse;
			converse = 0;
			for (final AlignmentListener listener : listeners) {
				converse = Math.max(listener.getMinConverse(context), converse);
			}
			if (converse == oldConverse)
				return;
		}
		submit(context);
	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
	}
}
