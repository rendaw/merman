package com.zarbosoft.bonestruct.wall.bricks;

import com.zarbosoft.bonestruct.display.Blank;
import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.BrickInterface;

public class BrickSpace extends Brick implements AlignmentListener {
	private int converse = 0;
	private Style.Baked style;
	private Alignment alignment;
	private final Blank visual;
	private int minConverse;

	public BrickSpace(final Context context, final BrickInterface inter) {
		super(inter);
		visual = context.display.blank();
	}

	@Override
	public void setStyle(final Context context, final Style.Baked style) {
		if (alignment != null)
			alignment.removeListener(context, this);
		alignment = inter.getAlignment(style);
		if (alignment != null)
			alignment.addListener(context, this);
		changed(context);
	}

	@Override
	public int converseEdge(final Context context) {
		return Math.max(Math.min(converse + style.space, context.edge), converse);
	}

	@Override
	public Properties properties(final Context context, final Style.Baked style) {
		return new Properties(
				style.broken,
				style.spaceTransverseBefore,
				style.spaceTransverseAfter,
				alignment,
				style.space + style.spaceBefore + style.spaceAfter
		);
	}

	@Override
	public DisplayNode getDisplayNode() {
		return visual;
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.minConverse = minConverse;
		this.converse = converse;
		visual.setPosition(context, new Vector(converse, 0), false);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {

	}

	@Override
	public void destroyed(final Context context) {
		super.destroyed(context);
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
