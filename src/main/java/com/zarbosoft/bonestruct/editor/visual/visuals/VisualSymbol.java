package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class VisualSymbol extends VisualPart implements ConditionAttachment.Listener, BrickInterface {
	private final FrontSymbol frontSymbol;
	public VisualParent parent;
	public Brick brick = null;
	public ConditionAttachment condition = null;

	public VisualSymbol(
			final Context context,
			final VisualParent parent,
			final FrontSymbol frontSymbol,
			final PSet<Tag> tags,
			final ConditionAttachment condition
	) {
		super(tags);
		this.parent = parent;
		this.frontSymbol = frontSymbol;
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

	@Override
	public VisualParent parent() {
		return parent;
	}

	@Override
	public boolean selectDown(final Context context) {
		return false;
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		if (brick != null)
			return brick;
		return createFirstBrick(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (brick != null)
			return null;
		if (condition != null && !condition.show())
			return null;
		brick = frontSymbol.type.createBrick(context, this);
		brick.setStyle(context, context.getStyle(tags(context)));
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
			brick.setStyle(context, context.getStyle(tags(context)));
		}
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		if (brick == null)
			return Iterables.concat();
		return Arrays.asList(new Pair<>(brick, brick.getPropertiesForTagsChange(context, change)));
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		// Nothing should be useful/change here
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (brick != null)
			brick.destroy(context);
		if (condition != null)
			condition.destroy(context);
	}

	@Override
	public boolean canCompact() {
		return false;
	}

	@Override
	public boolean canExpand() {
		return false;
	}

	@Override
	public VisualPart getVisual() {
		return this;
	}

	@Override
	public Brick createPrevious(final Context context) {
		return parent.createPreviousBrick(context);
	}

	@Override
	public Brick createNext(final Context context) {
		return parent.createNextBrick(context);
	}

	@Override
	public void brickDestroyed(final Context context) {
		brick = null;
	}

	@Override
	public Alignment getAlignment(final Style.Baked style) {
		return getAlignment(style.alignment);
	}

	@Override
	public Set<Tag> getTags(final Context context) {
		return tags(context);
	}
}
