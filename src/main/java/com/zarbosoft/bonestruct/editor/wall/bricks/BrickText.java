package com.zarbosoft.bonestruct.editor.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Font;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.syntax.style.Style;

public abstract class BrickText extends Brick implements AlignmentListener {
	Text text;
	private int minConverse;

	public BrickText(final Context context, final BrickInterface inter) {
		super(inter);
		text = context.display.text();
	}

	@Override
	public int converseEdge(final Context context) {
		return text.converseEdge(context);
	}

	public Properties properties(final Context context, final Style.Baked style) {
		final Font font = style.getFont(context);
		return new Properties(
				style.broken,
				font.getAscent(),
				font.getDescent(),
				inter.getAlignment(style),
				font.getWidth(text.text())
		);
	}

	public void setStyle(final Context context, final Style.Baked style) {
		this.style = style;
		if (text != null) {
			text.setColor(context, style.color);
			text.setFont(context, style.getFont(context));
		}
	}

	@Override
	public DisplayNode getDisplayNode() {
		return text;
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		this.minConverse = minConverse;
		text.setConverse(context, converse);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {
		text.setTransverse(context, ascent);
	}

	public void setText(final Context context, final String text) {
		this.text.setText(context, text.replaceAll("\\p{Cntrl}", context.syntax.placeholder));
		changed(context);
	}

	@Override
	public void align(final Context context) {
		changed(context);
	}

	@Override
	public int getConverse(final Context context) {
		return text.converse(context);
	}

	@Override
	public int getMinConverse(final Context context) {
		return minConverse;
	}

	public Font getFont() {
		return text.font();
	}

	public int getConverseOffset(final int index) {
		return text.getConverseAtIndex(index);
	}

	public int getUnder(final Context context, final Vector point) {
		return text.getIndexAtConverse(context, point.converse);
	}
}
