package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Iterator;
import java.util.Map;

public abstract class Visual {
	public abstract VisualParent parent();

	public abstract void globalTagsChanged(
			Context context
	);

	public abstract void changeTags(final Context context, final TagsChange change);

	public abstract Brick createOrGetFirstBrick(Context context);

	public abstract Brick createFirstBrick(Context context);

	public abstract Brick createLastBrick(Context context);

	public abstract Brick getFirstBrick(Context context);

	public abstract Brick getLastBrick(Context context);

	public Iterator<Visual> children() {
		return Iterators.forArray();
	}

	public abstract void compact(Context context);

	public abstract void expand(Context context);

	public abstract Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
			Context context, TagsChange change
	);

	public Alignment getAlignment(final String alignment) {
		if (parent() != null)
			return parent().getAlignment(alignment);
		return null;
	}

	public int depth() {
		final VisualParent parent = parent();
		if (parent == null)
			return 0;
		final VisualAtom atomVisual = parent.atomVisual();
		if (atomVisual == null)
			return 0;
		return atomVisual.depth;
	}

	public abstract void uproot(Context context, Visual root);

	public abstract void root(
			Context context, VisualParent parent, Map<String, Alignment> alignments, int depth
	);

	public abstract boolean selectDown(final Context context);

	public Hoverable hover(final Context context, final Vector point) {
		return parent().hover(context, point);
	}

	public void suggestCreateBricks(final Context context) {
		final Brick previousBrick = parent() == null ? null : parent().getPreviousBrick(context);
		final Brick nextBrick = parent() == null ? null : parent().getNextBrick(context);
		if (previousBrick != null && nextBrick != null)
			context.idleLayBricksAfterEnd(previousBrick);
	}

	public Map<String, Alignment> alignments() {
		if (parent() == null)
			return ImmutableMap.of();
		return parent().visual().alignments();
	}

}
