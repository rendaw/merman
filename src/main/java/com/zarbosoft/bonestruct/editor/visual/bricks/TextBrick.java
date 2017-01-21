package com.zarbosoft.bonestruct.editor.visual.bricks;

import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.raw.RawText;
import com.zarbosoft.bonestruct.editor.visual.raw.RawTextUtils;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import javafx.scene.Node;
import javafx.scene.text.Font;

public abstract class TextBrick extends Brick implements AlignmentListener {
	RawText text;
	private int minConverse;

	@Override
	public int converseEdge(final Context context) {
		return text.converseEdge(context);
	}

	public Properties properties(final Style.Baked style) {
		final Font font = style.getFont();
		return new Properties(
				style.broken,
				(int) RawTextUtils.getAscent(font),
				(int) RawTextUtils.getDescent(font),
				getAlignment(style),
				(int) RawTextUtils.computeTextWidth(font, text.getText())
		);
	}

	protected abstract Alignment getAlignment(Style.Baked style);

	public void setStyle(final Style.Baked style) {
		if (text != null)
			text.setStyle(style);
	}

	@Override
	public Node getRawVisual() {
		return text.getVisual();
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
		if (this.text == null)
			this.text = new RawText(context, getStyle());
		this.text.setText(context, text.replaceAll("\\p{Cntrl}", "\u25A2"));
		changed(context);
	}

	protected abstract Style.Baked getStyle();

	@Override
	public Properties properties() {
		return properties(getStyle());
	}

	@Override
	public void align(final Context context) {
		changed(context);
	}

	@Override
	public int getConverse(final Context context) {
		return text.getConverse(context);
	}

	@Override
	public int getMinConverse(final Context context) {
		return minConverse;
	}

	public Font getFont() {
		return text.getFont();
	}

	public int getConverseOffset(final int index) {
		return text.getConverseOffset(index);
	}

	public int getUnder(final Context context, final Vector point) {
		return text.getUnder(context, point.converse, context.edge);
	}
}
