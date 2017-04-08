package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.bricks.BrickImage;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Arrays;
import java.util.Set;

public class VisualImage extends VisualNodePart {
	public VisualNodeParent parent;
	public BrickImage brick = null;

	public VisualImage(final Set<Tag> tags) {
		super(tags);
	}

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	@Override
	public boolean select(final Context context) {
		return false;
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (brick != null)
			throw new AssertionError("Brick should be initially empty or cleared after being deleted");
		brick = new BrickImage(this, context);
		return brick;
	}

	@Override
	public Brick createLastBrick(final Context context) {
		return createFirstBrick(context);
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		return brick;
	}

	@Override
	public Brick getLastBrick(final Context context) {
		return brick;
	}

	@Override
	public void changeTags(final Context context, final TagsChange tagsChange) {
		super.changeTags(context, tagsChange);
		if (brick != null) {
			brick.setStyle(context);
		}
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		if (brick == null)
			return Iterables.concat();
		return Arrays.asList(new Pair<Brick, Brick.Properties>(brick,
				brick.getPropertiesForTagsChange(context, change)
		));
	}

	@Override
	public void destroy(final Context context) {
		if (brick != null)
			brick.destroy(context);
	}

}
