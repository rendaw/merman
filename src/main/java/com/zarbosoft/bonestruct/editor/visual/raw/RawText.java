package com.zarbosoft.bonestruct.editor.visual.raw;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.Style;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class RawText {
	protected final Text text = new Text();

	public RawText(final Context context, final Style.Baked style) {
		text.setTextOrigin(VPos.BASELINE);
		text.setFont(style.getFont());
		text.setFill(style.color);
	}

	public void setStyle(final Style.Baked style) {
		text.setFont(style.getFont());
		text.setFill(style.color);
	}

	public void setText(final Context context, final String newText) {
		final Vector at = context.sceneGet(text);
		text.setText(newText);
		context.translate(text, at, false);
	}

	public String getText() {
		return text.getText();
	}

	public int getUnder(final Context context, final int converse, final int edge) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return text
						.impl_hitTestChar(new Point2D(text.getX(), converse - getConverse(context)))
						.getInsertionIndex();
			case LEFT:
			case RIGHT:
				return text
						.impl_hitTestChar(new Point2D(converse - getConverse(context), text.getY()))
						.getInsertionIndex();
		}
		throw new AssertionError("DEAD CODE");
	}

	public int converseSpan(final Context context) {
		return context.sceneGetConverseSpan(text);
	}

	public void setConverse(final Context context, final int converse) {
		context.translateConverse(text, converse, false);
	}

	public int getConverse(final Context context) {
		return context.sceneGetConverse(text);
	}

	public int converseEdge(final Context context) {
		return getConverse(context) + converseSpan(context);
	}

	public int transverseSpan(final Context context) {
		return context.sceneGetTransverseSpan(text);
	}

	public void setTransverse(final Context context, final int transverse) {
		setTransverse(context, transverse, false);
	}

	public void setTransverse(final Context context, final int transverse, final boolean animate) {
		context.translateTransverse(text, transverse, animate);
	}

	protected int getTransverse(final Context context) {
		return context.sceneGetTransverse(text);
	}

	public Node getVisual() {
		return text;
	}

	public Font getFont() {
		return text.getFont();
	}

	public int getConverseOffset(final int index) {
		if (index == 0)
			return 0;
		text.getText().substring(0, 0);
		final double precedingLength =
				RawTextUtils.computeTextWidth(text.getFont(), text.getText().substring(0, index));
		final double charLength = RawTextUtils.computeTextWidth(
				text.getFont(),
				text.getText().substring(Math.max(0, index - 1), Math.min(text.getText().length(), Math.max(1, index)))
		);
		return (int) (precedingLength - charLength * 0.2);
	}
}
