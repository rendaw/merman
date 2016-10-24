package com.zarbosoft.bonestruct.visual.alignment;

import com.zarbosoft.bonestruct.visual.Context;

public class ConcensusAlignment extends Alignment {
	@Override
	public void set(final Context context, final int position) {
		if (position > this.converse) {
			this.converse = position;
		} else {
			converse = 0;
			for (final AlignmentListener listener : listeners) {
				converse = Math.max(listener.getConverse(context), converse);
			}
		}
		submit(context);
	}

	@Override
	public void place(final Context context, final Alignment parent) {
	}
}
