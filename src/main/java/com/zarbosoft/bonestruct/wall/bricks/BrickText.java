package com.zarbosoft.bonestruct.wall.bricks;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Font;
import com.zarbosoft.bonestruct.display.Text;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;

public abstract class BrickText extends Brick implements AlignmentListener {
	Text text;
	private int minConverse;

	BrickText(final Context context) {
		text = context.display.text();
	}

	@Override
	public int converseEdge(final Context context) {
		return text.converseEdge(context);
	}

	public Properties properties(final Context context, final Style.Baked style) {
		final Font font = style.getFont(context);
		return new Properties(style.broken,
				font.getAscent(),
				font.getDescent(),
				getAlignment(style),
				font.getWidth(text.text())
		);
	}

	protected abstract Alignment getAlignment(Style.Baked style);

	public void setStyle(final Context context, final Style.Baked style) {
		if (text != null) {
			text.setColor(context, style.color);
			text.setFont(context, style.getFont(context));
		}
	}

	@Override
	public DisplayNode getRawVisual() {
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
		this.text.setText(context, text.replaceAll("\\p{Cntrl}", "\u25A2"));
		changed(context);
	}

	protected abstract Style.Baked getStyle();

	@Override
	public Properties properties(final Context context) {
		return properties(context, getStyle());
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
