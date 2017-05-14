package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.rendaw.common.DeadCode;
import org.pcollections.PSet;

public abstract class VisualPart extends Visual {
	public VisualPart(final PSet<Tag> tags) {
		super(tags);
	}

	@Override
	public int spacePriority() {
		throw new DeadCode();
	}

	@Override
	public boolean canCompact() {
		throw new DeadCode();
	}

	@Override
	public boolean canExpand() {
		throw new DeadCode();
	}

	@Override
	public void compact(final Context context) {
		changeTags(
				context,
				new TagsChange(ImmutableSet.of(new StateTag("compact")), ImmutableSet.of(new StateTag("expanded")))
		);
	}

	@Override
	public void expand(final Context context) {
		changeTags(
				context,
				new TagsChange(ImmutableSet.of(new StateTag("expanded")), ImmutableSet.of(new StateTag("compact")))
		);
	}

	public Hoverable hover(final Context context, final Vector point) {
		return parent().hover(context, point);
	}
}
