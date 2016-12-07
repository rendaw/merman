package com.zarbosoft.bonestruct.visual.alignment;

import com.zarbosoft.bonestruct.visual.Context;

import java.util.Map;

public class ConcensusAlignment extends Alignment {
	@Override
	public void set(final Context context, final int gotConverse) {
		if (gotConverse > this.converse) {
			this.converse = gotConverse;
		} else {
			converse = 0;
			for (final AlignmentListener listener : listeners) {
				converse = Math.max(listener.getConverse(context), converse);
			}
		}
		submit(context);
	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
	}
}
