package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Image;
import com.zarbosoft.merman.editor.visual.AlignmentListener;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;

import java.nio.file.Paths;

public class BrickImage extends Brick implements AlignmentListener {
	private final Image image;
	private Style.Baked style;

	public BrickImage(final Context context, final BrickInterface inter) {
		super(inter);
		image = context.display.image();
		tagsChanged(context);
	}

	@Override
	public void tagsChanged(final Context context) {
		this.style = context.getStyle(context.globalTags.plusAll(inter.getTags(context)));
		if (alignment != null)
			alignment.removeListener(context, this);
		alignment = inter.getAlignment(style);
		if (alignment != null)
			alignment.addListener(context, this);
		image.setImage(context, Paths.get(style.image));
		image.rotate(context, style.rotate);
		changed(context);
	}

	public Properties properties(final Context context, final Style.Baked style) {
		return new Properties(
				style.split,
				(int) image.transverseSpan(context),
				(int) 0,
				inter.getAlignment(style),
				(int) image.converseSpan(context)
		);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {
		image.setTransverse(context, ascent, false);
	}

	@Override
	public void destroyed(final Context context) {
		super.destroyed(context);
		if (alignment != null)
			alignment.removeListener(context, this);
	}

	@Override
	public int converseEdge(final Context context) {
		return image.converseEdge(context);
	}

	@Override
	public int getConverse(final Context context) {
		return image.converse(context);
	}

	@Override
	public DisplayNode getDisplayNode() {
		return image;
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.minConverse = minConverse;
		image.setConverse(context, converse, false);
	}
}
