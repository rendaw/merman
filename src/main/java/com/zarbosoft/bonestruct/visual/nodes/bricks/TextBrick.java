package com.zarbosoft.bonestruct.visual.nodes.bricks;

import com.zarbosoft.bonestruct.visual.Brick;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Style;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.alignment.AlignmentListener;
import com.zarbosoft.bonestruct.visual.nodes.parts.RawText;
import com.zarbosoft.bonestruct.visual.nodes.parts.RawTextUtils;
import javafx.scene.Node;
import javafx.scene.text.Font;

public abstract class TextBrick extends Brick implements AlignmentListener {
	RawText text;

	@Override
	public int converseEdge(final Context context) {
		return text.converseEdge(context.edge);
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
	public void setConverse(final Context context, final int converse) {
		text.setConverse(converse, context.edge);
	}

	@Override
	public void allocateTransverse(final Context context, final int ascent, final int descent) {
		text.setTransverse(ascent, context.transverseEdge);
	}

	public void setText(final Context context, final String text) {
		if (this.text == null)
			this.text = RawText.create(context, getStyle());
		this.text.setText(text);
	}

	protected abstract Style.Baked getStyle();

	public String getText() {
		return text.getText();
	}

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
		return text.getConverse(context.edge);
	}
}
