package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.Map;

public class RelativeAlignmentImplementation extends Alignment implements AlignmentListener {
	private final String base;
	private final int offset;
	private Alignment alignment;

	public RelativeAlignmentImplementation(final String base, final int offset) {
		this.base = base;
		this.offset = offset;
		converse = offset;
	}

	@Override
	public void set(final Context context, final int position) {

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
