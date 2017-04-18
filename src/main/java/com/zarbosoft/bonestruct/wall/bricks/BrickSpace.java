package com.zarbosoft.bonestruct.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.*;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualSpace;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.pcollections.HashTreePSet;

import java.util.HashSet;
import java.util.Set;

public class BrickSpace extends Brick implements AlignmentListener {
	private final VisualSpace spaceVisual;
	private int converse = 0;
	private Style.Baked style;
	private Alignment alignment;
	private final Region visual = new Region();
	private int minConverse;

	public BrickSpace(final VisualSpace spaceVisual, final Context context) {
		this.spaceVisual = spaceVisual;
		setStyle(context);
	}

	public void setStyle(final Context context) {
		this.style = context.getStyle(HashTreePSet.from(spaceVisual.tags(context)).plusAll(context.globalTags));
		if (alignment != null)
			alignment.removeListener(context, this);
		alignment = spaceVisual.getAlignment(style.alignment);
		if (alignment != null)
			alignment.addListener(context, this);
		changed(context);
	}

	@Override
	public int converseEdge(final Context context) {
		return Math.max(Math.min(converse + style.space, context.edge), converse);
	}

	@Override
	public VisualPart getVisual() {
		return spaceVisual;
	}

	@Override
	public Properties getPropertiesForTagsChange(final Context context, final Visual.TagsChange change) {
		final Set<Visual.Tag> tags = new HashSet<>(spaceVisual.tags(context));
		tags.removeAll(change.remove);
		tags.addAll(change.add);
		return properties(context.getStyle(tags));
	}

	@Override
	public Properties properties() {
		return properties(style);
	}

	public Properties properties(final Style.Baked style) {
		return new Properties(
				style.broken,
				style.spaceTransverseBefore,
				style.spaceTransverseAfter,
				alignment,
				style.space + style.spaceBefore + style.spaceAfter
		);
	}

	@Override
	public Node getRawVisual() {
		return visual;
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.minConverse = minConverse;
		this.converse = converse;
		context.translate(visual, new Vector(converse, 0));
	}

	@Override
	public Brick createNext(final Context context) {
		return spaceVisual.parent.createNextBrick(context);
	}

	@Override
	public Brick createPrevious(final Context context) {
		return spaceVisual.parent.createPreviousBrick(context);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {

	}

	@Override
	public void destroyed(final Context context) {
		spaceVisual.brick = null;
		if (alignment != null)
			alignment.removeListener(context, this);
	}

	@Override
	public void align(final Context context) {
		changed(context);
	}

	@Override
	public int getConverse(final Context context) {
		return converse;
	}

	@Override
	public int getMinConverse(final Context context) {
		return minConverse;
	}
}
