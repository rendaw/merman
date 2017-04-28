package com.zarbosoft.bonestruct.display.javafx;

import com.zarbosoft.bonestruct.display.Font;
import com.zarbosoft.bonestruct.display.Text;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;

public class JavaFXText extends JavaFXNode implements Text {
	protected final javafx.scene.text.Text text = new javafx.scene.text.Text();

	public JavaFXText() {
		text.setTextOrigin(VPos.BASELINE);
	}

	@Override
	public String text() {
		return text.getText();
	}

	public void setText(final Context context, final String newText) {
		final Vector at = position(context);
		text.setText(newText);
		translate(context, at, false);
	}

	@Override
	public void setColor(final Context context, final ModelColor color) {
		text.setFill(Helper.convert(color));
	}

	@Override
	public Font font() {
		return new JavaFXFont(text.getFont());
	}

	@Override
	public void setFont(final Context context, final Font font) {
		text.setFont(((JavaFXFont) font).font);
	}

	public String getText() {
		return text.getText();
	}

	@Override
	protected Node node() {
		return text;
	}

	public int getIndexAtConverse(final Context context, final int converse) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return text.hitTest(new Point2D(text.getX(), converse - converse(context))).getInsertionIndex();
			case LEFT:
			case RIGHT:
				return text.hitTest(new Point2D(converse - converse(context), text.getY())).getInsertionIndex();
		}
		throw new DeadCode();
	}

	public int getConverseAtIndex(final int index) {
		if (index == 0)
			return 0;
		text.getText().substring(0, 0);
		final Font font = font();
		final double precedingLength = font.getWidth(text.getText().substring(0, index));
		final double charLength = font.getWidth(text
				.getText()
				.substring(Math.max(0, index - 1), Math.min(text.getText().length(), Math.max(1, index))));
		return (int) (precedingLength - charLength * 0.2);
	}
}
