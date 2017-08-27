package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;

import java.util.Map;

public class RelativeAlignment extends Alignment implements AlignmentListener {
	private final String base;
	private final int offset;
	private Alignment alignment;

	public RelativeAlignment(final String base, final int offset) {
		this.base = base;
		this.offset = offset;
		converse = offset;
	}

	@Override
	public void feedback(final Context context, final int position) {

	}

	@Override
	public void root(final Context context, final Map<String, Alignment> parents) {
		if (alignment != null) {
			alignment.removeListener(context, this);
		}
		alignment = parents.get(base);
		if (alignment == this)
			throw new AssertionError("Alignment parented to self");
		if (alignment != null)
			alignment.addListener(context, this);
		align(context);
	}

	@Override
	public void destroy(final Context context) {

	}

	@Override
	public void align(final Context context) {
		converse = (alignment == null ? 0 : alignment.converse) + offset;
		submit(context);
	}

	@Override
	public int getMinConverse(final Context context) {
		return converse;
	}

	@Override
	public String toString() {
		return String.format("relative-%d-p-%s", converse, alignment);
	}
}
