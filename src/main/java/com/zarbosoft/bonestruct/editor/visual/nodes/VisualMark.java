package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.bonestruct.syntax.front.FrontMark;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.bricks.BrickMark;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Arrays;
import java.util.Set;

public class VisualMark extends VisualPart implements ConditionAttachment.Listener {
	private final FrontMark frontMark;
	public VisualParent parent;
	public BrickMark brick = null;
	public ConditionAttachment condition = null;

	public VisualMark(final FrontMark frontMark, final Set<Tag> tags, final ConditionAttachment condition) {
		super(tags);
		this.frontMark = frontMark;
		if (condition != null) {
			this.condition = condition;
			condition.register(this);
		}
	}

	@Override
	public void conditionChanged(final Context context, final boolean show) {
		if (show) {
			suggestCreateBricks(context);
		} else if (brick != null) {
			brick.destroy(context);
		}
	}

	public void setText(final Context context, final String value) {
		if (brick != null)
			brick.setText(context, value);
	}

	@Override
	public void setParent(final VisualParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualParent parent() {
		return parent;
	}

	@Override
	public boolean selectDown(final Context context) {
		return false;
	}

	@Override
	public void select(final Context context) {
		throw new DeadCode();
	}

	@Override
	public void selectUp(final Context context) {
		throw new DeadCode();
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (brick != null)
			return null;
		if (condition != null && !condition.show())
			return null;
		brick = new BrickMark(this, context);
		brick.setText(context, frontMark.value);
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
	public void tagsChanged(final Context context) {
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
		if (condition != null)
			condition.destroy(context);
	}

	@Override
	public boolean isAt(final Value value) {
		return false;
	}

	@Override
	public boolean canCompact() {
		return false;
	}

	@Override
	public boolean canExpand() {
		return false;
	}
}
