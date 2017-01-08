package com.zarbosoft.bonestruct.editor.visual.raw;

import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.visual.Context;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class RawText {
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

	public static RawText create(final Context context, final Style.Baked style) {
		switch (context.syntax.converseDirection) {
			case UP:
				switch (context.syntax.transverseDirection) {
					case UP:
					case DOWN:
						throw new AssertionError("dead code");
					case LEFT:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(text.getX(), getConverse(edge) - converse))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutY(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutY();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								text.setLayoutY(text.getLayoutY() + oldSpan - converseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutX(edge - transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return edge - (int) text.getLayoutX();
							}
						};
					case RIGHT:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(text.getX(), getConverse(edge) - converse))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutY(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutY();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								text.setLayoutY(text.getLayoutY() + oldSpan - converseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutX(transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return (int) text.getLayoutX();
							}
						};
				}
			case DOWN:
				switch (context.syntax.transverseDirection) {
					case UP:
					case DOWN:
						throw new AssertionError("dead code");
					case LEFT:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(text.getX(), converse - getConverse(edge)))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutY(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) text.getLayoutY();
							}

							public void setText(final String newText) {
								text.setText(newText);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutX(edge - transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return edge - (int) text.getLayoutX();
							}
						};
					case RIGHT:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(text.getX(), converse - getConverse(edge)))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutY(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) text.getLayoutY();
							}

							public void setText(final String newText) {
								text.setText(newText);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutX(transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return (int) text.getLayoutX();
							}
						};
				}
			case LEFT:
				switch (context.syntax.transverseDirection) {
					case UP:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(getConverse(edge) - converse, text.getY()))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutX(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutX();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								text.setLayoutX(text.getLayoutX() + oldSpan - converseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutY(edge - transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return edge - (int) text.getLayoutY();
							}
						};
					case DOWN:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(getConverse(edge) - converse, text.getY()))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutX(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutX();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								text.setLayoutX(text.getLayoutX() + oldSpan - converseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutY(transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return (int) text.getLayoutY();
							}
						};
					case LEFT:
					case RIGHT:
						throw new AssertionError("dead code");
				}
			case RIGHT:
				switch (context.syntax.transverseDirection) {
					case UP:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(converse - getConverse(edge), text.getY()))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutX(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) text.getLayoutX();
							}

							public void setText(final String newText) {
								text.setText(newText);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutY(edge - transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return edge - (int) text.getLayoutY();
							}
						};
					case DOWN:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text
										.impl_hitTestChar(new Point2D(converse - getConverse(edge), text.getY()))
										.getInsertionIndex();
							}

							@Override
							public int converseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								text.setLayoutX(converse);
							}

							public void setText(final String newText) {
								text.setText(newText);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) text.getLayoutX();
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								text.setLayoutY(transverse);
							}

							@Override
							protected int getTransverse(final int edge) {
								return (int) text.getLayoutY();
							}
						};
					case LEFT:
					case RIGHT:
						throw new AssertionError("dead code");
				}
		}
		throw new AssertionError("dead code");
	}

	public abstract void setText(final String newText);

	public String getText() {
		return text.getText();
	}

	public abstract int getUnder(int converse, int edge);

	public abstract int converseSpan();

	public abstract void setConverse(int converse, int edge);

	public abstract int getConverse(int edge);

	public int converseEdge(final int edge) {
		return getConverse(edge) + converseSpan();
	}

	public abstract int transverseSpan();

	public abstract void setTransverse(int transverse, int edge);

	protected abstract int getTransverse(int edge);

	public Node getVisual() {
		return text;
	}

	public Font getFont() {
		return text.getFont();
	}

	public int getConverseOffset(final int index) {
		return (int) (
				RawTextUtils.computeTextWidth(text.getFont(), text.getText().substring(0, index)) -
						RawTextUtils.computeTextWidth(text.getFont(), text.getText().substring(Math.max(0, index - 1),
								Math.min(text.getText().length(), Math.max(1, index))
						)) * 0.2

		);
	}
}
