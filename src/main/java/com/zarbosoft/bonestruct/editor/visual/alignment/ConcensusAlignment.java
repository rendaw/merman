package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;

import java.util.Map;

public class ConcensusAlignment extends Alignment {
	@Override
	public void feedback(final Context context, final int gotConverse) {
		final int oldConverse = this.converse;
		if (gotConverse == this.converse)
			return;
		if (gotConverse > this.converse) {
			this.converse = gotConverse;
		} else {
			reduce(context);
		}
		if (this.converse != oldConverse)
			submit(context);
	}

	@Override
	public void removeListener(
			final Context context, final AlignmentListener listener
	) {
		super.removeListener(context, listener);
		if (listener.getMinConverse(context) == converse) {
			final int oldConverse = converse;
			reduce(context);
			if (converse != oldConverse)
				submit(context);
		}
	}

	private void reduce(final Context context) {
		converse = 0;
		System.out.format("Reducing\n");
		for (final AlignmentListener listener : listeners) {
			System.out.format("\tl %s\n", listener.getMinConverse(context));
			converse = Math.max(listener.getMinConverse(context), converse);
		}
	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
	}
}
