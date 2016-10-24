package com.zarbosoft.bonestruct.visual.alignment;

import com.zarbosoft.bonestruct.visual.Context;

public class RelativeAlignment extends Alignment implements AlignmentListener {
	private final String base;
	private final int offset;
	private Alignment alignment;

	public RelativeAlignment(final String base, final int offset) {
		this.base = base;
		this.offset = offset;
	}

	@Override
	public void set(final Context context, final int position) {

	}

	@Override
	public void place(final Context context, final Alignment parent) {
		alignment = parent;
		align(context);
	}

	@Override
	public void align(final Context context) {
		this.converse = this.alignment.converse + offset;
		submit(context);
	}

	@Override
	public int getConverse(final Context context) {
		return 0;
	}
}
