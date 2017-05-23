package com.zarbosoft.bonestruct.editor.display.javafx;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.Font;
import com.zarbosoft.bonestruct.editor.display.Text;
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
		setPosition(context, at, false);
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

	@Override
	public int converse(final Context context) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return (int) text.getLayoutY();
			case LEFT:
			case RIGHT:
				return (int) text.getLayoutX();
			default:
				throw new DeadCode();
		}
	}

	@Override
	public int transverse(final Context context) {
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				return (int) text.getLayoutY();
			case LEFT:
			case RIGHT:
				return (int) text.getLayoutX();
			default:
				throw new DeadCode();
		}
	}

	@Override
	public Vector position(final Context context) {
		final int converse;
		final int transverse;
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				converse = (int) text.getLayoutY();
				break;
			case LEFT:
			case RIGHT:
				converse = (int) text.getLayoutX();
				break;
			default:
				throw new DeadCode();
		}
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				transverse = (int) text.getLayoutY();
				break;
			case LEFT:
			case RIGHT:
				transverse = (int) text.getLayoutX();
				break;
			default:
				throw new DeadCode();
		}
		return new Vector(converse, transverse);
	}

	@Override
	public void setConverse(final Context context, final int converse, final boolean animate) {
		Integer x = null;
		Integer y = null;
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				y = converse;
				break;
			case LEFT:
			case RIGHT:
				x = converse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(text, x - node().getLayoutX(), null).play();
			node().setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(text, null, y - node().getLayoutY()).play();
			node().setLayoutY(y);
		}
	}

	@Override
	public void setTransverse(final Context context, final int transverse, final boolean animate) {
		Integer x = null;
		Integer y = null;
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				y = transverse;
				break;
			case LEFT:
			case RIGHT:
				x = transverse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(text, x - node().getLayoutX(), null).play();
			node().setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(text, null, y - node().getLayoutY()).play();
			node().setLayoutY(y);
		}
	}

	@Override
	public void setPosition(final Context context, final Vector vector, final boolean animate) {
		int x = 0;
		int y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				y = -vector.converse - (int) node().getLayoutBounds().getHeight();
				break;
			case DOWN:
				y = vector.converse;
				break;
			case LEFT:
				x = -vector.converse - (int) node().getLayoutBounds().getWidth();
				break;
			case RIGHT:
				x = vector.converse;
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				y = -vector.transverse - (int) node().getLayoutBounds().getHeight();
				break;
			case DOWN:
				y = vector.transverse;
				break;
			case LEFT:
				x = -vector.transverse - (int) node().getLayoutBounds().getWidth();
				break;
			case RIGHT:
				x = vector.transverse;
				break;
		}
		if (animate)
			new TransitionSmoothOut(text, x - node().getLayoutX(), y - node().getLayoutY()).play();
		node().setLayoutX(x);
		node().setLayoutY(y);
	}
}
