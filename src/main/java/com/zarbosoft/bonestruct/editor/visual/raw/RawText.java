package com.zarbosoft.bonestruct.editor.visual.raw;

import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.visual.Context;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
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
								setLayoutY(text, edge - converse - converseSpan(), false);
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutY();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								setLayoutY(text, text.getLayoutY() + oldSpan - converseSpan(), false);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutX(text, edge - transverse, animate);
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
								setLayoutY(text, edge - converse - converseSpan(), false);
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutY();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								setLayoutY(text, text.getLayoutY() + oldSpan - converseSpan(), false);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutX(text, transverse, animate);
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
								setLayoutY(text, converse, false);
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
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutX(text, edge - transverse, animate);
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
								setLayoutY(text, converse, false);
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
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutX(text, transverse, animate);
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
								setLayoutX(text, edge - converse - converseSpan(), false);
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutX();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								setLayoutX(text, text.getLayoutX() + oldSpan - converseSpan(), false);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutY(text, edge - transverse, animate);
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
								setLayoutX(text, edge - converse - converseSpan(), false);
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) text.getLayoutX();
							}

							public void setText(final String newText) {
								final int oldSpan = converseSpan();
								text.setText(newText);
								setLayoutX(text, text.getLayoutX() + oldSpan - converseSpan(), false);
							}

							@Override
							public int transverseSpan() {
								return (int) text.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutY(text, transverse, animate);
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
								setLayoutX(text, converse, false);
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
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutY(text, edge - transverse, animate);
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
								setLayoutX(text, converse, false);
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
							public void setTransverse(final int transverse, final int edge, final boolean animate) {
								setLayoutY(text, transverse, animate);
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

	private static void setLayoutY(final Text text, final double y, final boolean animate) {
		if (animate) {
			final TranslateTransition translation = new TranslateTransition(javafx.util.Duration.seconds(1), text);
			//translation.setInterpolator(interpolator);
			translation.setInterpolator(Interpolator.SPLINE(0, 0.5, 1, 1));
			translation.setToY(y);
			translation.play();
		} else {
			text.setLayoutY(y);
		}
	}

	private static void setLayoutX(final Text text, final double x, final boolean animate) {
		if (animate) {
			final TranslateTransition translation = new TranslateTransition(javafx.util.Duration.seconds(1), text);
			//translation.setInterpolator(interpolator);
			translation.setInterpolator(Interpolator.SPLINE(0, 0.5, 1, 1));
			translation.setToX(x);
			translation.play();
		} else {
			text.setLayoutX(x);
		}
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

	public void setTransverse(final int transverse, final int edge) {
		setTransverse(transverse, edge, false);
	}

	public abstract void setTransverse(int transverse, int edge, boolean animate);

	protected abstract int getTransverse(int edge);

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
