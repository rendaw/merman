package com.zarbosoft.bonestruct.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualImage;
import com.zarbosoft.bonestruct.editor.visual.raw.RawImage;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import javafx.scene.Node;

import java.util.HashSet;
import java.util.Set;

public class BrickImage extends Brick implements AlignmentListener {
	private final VisualImage imageVisual;
	private final RawImage image;
	private Style.Baked style;
	private Alignment alignment;
	private int minConverse;

	public BrickImage(final VisualImage imageVisual, final Context context) {
		this.imageVisual = imageVisual;
		setStyle(context);
		image = RawImage.create(context, style);
	}

	public void setStyle(final Context context) {
		style = context.getStyle(imageVisual.tags(context));
		if (alignment != null)
			alignment.removeListener(context, this);
		alignment = imageVisual.getAlignment(style.alignment);
		if (alignment != null)
			alignment.addListener(context, this);
		image.setStyle(style);
		changed(context);
	}

	public Properties properties(final Style.Baked style) {
		return new Properties(
				style.broken,
				(int) image.transverseSpan(),
				(int) 0,
				imageVisual.getAlignment(style.alignment),
				(int) image.converseSpan()
		);
	}

	@Override
	public Brick createNext(final Context context) {
		return imageVisual.parent.createNextBrick(context);
	}

	@Override
	public Brick createPrevious(final Context context) {
		return imageVisual.parent.createPreviousBrick(context);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {
		image.setTransverse(ascent, context.transverseEdge);
	}

	@Override
	public void destroyed(final Context context) {
		imageVisual.brick = null;
		if (alignment != null)
			alignment.removeListener(context, this);
	}

	@Override
	public int converseEdge(final Context context) {
		return image.converseEdge(context.edge);
	}

	@Override
	public VisualPart getVisual() {
		return imageVisual;
	}

	@Override
	public Properties getPropertiesForTagsChange(
			final Context context, final Visual.TagsChange change
	) {
		final Set<Visual.Tag> tags = new HashSet<>(imageVisual.tags(context));
		tags.removeAll(change.remove);
		tags.addAll(change.add);
		return properties(context.getStyle(tags));
	}

	@Override
	public int getConverse(final Context context) {
		return image.getConverse(context.edge);
	}

	@Override
	public int getMinConverse(final Context context) {
		return minConverse;
	}

	@Override
	public Properties properties() {
		return properties(style);
	}

	@Override
	public Node getRawVisual() {
		return image.getVisual();
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.minConverse = minConverse;
		image.setConverse(converse, context.edge);
	}

	@Override
	public void align(final Context context) {
		changed(context);
	}
}
