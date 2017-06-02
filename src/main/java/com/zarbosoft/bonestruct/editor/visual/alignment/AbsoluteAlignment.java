package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;

import java.util.Map;

public class AbsoluteAlignment extends Alignment implements AlignmentListener {
	public AbsoluteAlignment(final int offset) {
		converse = offset;
	}

	@Override
	public void feedback(final Context context, final int position) {

	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
		align(context);
	}

	@Override
	public void align(final Context context) {
		submit(context);
	}

	@Override
	public int getMinConverse(final Context context) {
		return converse;
	}

	@Override
	public String toString() {
		return String.format("absolute-%s", converse);
	}
}
