package com.zarbosoft.bonestruct.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualPrimitive;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;

import java.util.HashSet;
import java.util.Set;

public class LineBrick extends BrickText {
	private final VisualPrimitive visualPrimitive;
	private final VisualPrimitive.Line line;
	private final VisualPrimitive.BrickStyle style;

	public LineBrick(
			final VisualPrimitive visualPrimitive,
			final VisualPrimitive.Line line,
			final VisualPrimitive.BrickStyle style
	) {
		this.visualPrimitive = visualPrimitive;
		this.line = line;
		this.style = style;
	}

	@Override
	public VisualPart getVisual() {
		return visualPrimitive;
	}

	@Override
	public Properties getPropertiesForTagsChange(
			final Context context, final Visual.TagsChange change
	) {
		final Set<Visual.Tag> tags = new HashSet<>(line.hard ? visualPrimitive.hardTags : visualPrimitive.softTags);
		tags.removeAll(change.remove);
		tags.addAll(change.add);
		return properties(context.getStyle(tags));
	}

	@Override
	public Brick createNext(final Context context) {
		return line.createNextBrick(context);
	}

	@Override
	public Brick createPrevious(final Context context) {
		return line.createPreviousBrick(context);
	}

	@Override
	public void destroyed(final Context context) {
		line.brick = null;
	}

	@Override
	protected Alignment getAlignment(final Style.Baked style) {
		return line.index == 0 ?
				this.style.firstAlignment :
				line.hard ? this.style.hardAlignment : this.style.softAlignment;
	}

	@Override
	protected Style.Baked getStyle() {
		return line.index == 0 ? style.firstStyle : line.hard ? style.hardStyle : style.softStyle;
	}

	@Override
	public Hoverable hover(final Context context, final Vector point) {
		return line.hover(context, point);
	}
}
