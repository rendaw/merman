package com.zarbosoft.bonestruct.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualMark;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;

import java.util.HashSet;
import java.util.Set;

public class BrickMark extends BrickText {
	private final VisualMark markVisual;
	private Style.Baked style;
	private Alignment alignment;

	public BrickMark(final VisualMark markVisual, final Context context) {
		this.markVisual = markVisual;
		setStyle(context);
	}

	public void setStyle(final Context context) {
		style = context.getStyle(markVisual.tags(context));
		if (alignment != null)
			alignment.removeListener(context, this);
		alignment = markVisual.getAlignment(style.alignment);
		if (alignment != null)
			alignment.addListener(context, this);
		changed(context);
		super.setStyle(style);
	}

	@Override
	protected Alignment getAlignment(final Style.Baked style) {
		return markVisual.getAlignment(style.alignment);
	}

	@Override
	protected Style.Baked getStyle() {
		return style;
	}

	@Override
	public Brick createNext(final Context context) {
		return markVisual.parent.createNextBrick(context);
	}

	@Override
	public Brick createPrevious(final Context context) {
		return markVisual.parent.createPreviousBrick(context);
	}

	@Override
	public void destroyed(final Context context) {
		markVisual.brick = null;
		if (alignment != null)
			alignment.removeListener(context, this);
	}

	@Override
	public VisualNodePart getVisual() {
		return markVisual;
	}

	@Override
	public Properties getPropertiesForTagsChange(
			final Context context, final VisualNode.TagsChange change
	) {
		final Set<VisualNode.Tag> tags = new HashSet<>(markVisual.tags(context));
		tags.removeAll(change.remove);
		tags.addAll(change.add);
		return properties(context.getStyle(tags));
	}
}
