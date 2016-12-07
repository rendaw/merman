package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Style;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
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
								return text.impl_hitTestChar(new Point2D(text.getX(), edge - converse)).getCharIndex();
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
								text.setLayoutX(edge - transverse - transverseSpan());
							}
						};
					case RIGHT:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text.impl_hitTestChar(new Point2D(text.getX(), edge - converse)).getCharIndex();
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
								return text.impl_hitTestChar(new Point2D(text.getX(), converse)).getCharIndex();
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
								text.setLayoutX(edge - transverse - transverseSpan());
							}
						};
					case RIGHT:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text.impl_hitTestChar(new Point2D(text.getX(), converse)).getCharIndex();
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
						};
				}
			case LEFT:
				switch (context.syntax.transverseDirection) {
					case UP:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text.impl_hitTestChar(new Point2D(edge - converse, text.getY())).getCharIndex();
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
								text.setLayoutY(edge - transverse - transverseSpan());
							}
						};
					case DOWN:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text.impl_hitTestChar(new Point2D(edge - converse, text.getY())).getCharIndex();
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
								return text.impl_hitTestChar(new Point2D(converse, text.getY())).getCharIndex();
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
								text.setLayoutY(edge - transverse - transverseSpan());
							}
						};
					case DOWN:
						return new RawText(context, style) {
							@Override
							public int getUnder(final int converse, final int edge) {
								return text.impl_hitTestChar(new Point2D(converse, text.getY())).getCharIndex();
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
						};
					case LEFT:
					case RIGHT:
						throw new AssertionError("dead code");
				}
		}
		throw new AssertionError("dead code");
	}

	public abstract int getUnder(int converse, int edge);

	public abstract int converseSpan();

	public abstract void setConverse(int converse, int edge);

	public abstract int transverseSpan();

	public abstract void setTransverse(int transverse, int edge);

	public abstract void setText(final String newText);

	public String getText() {
		return text.getText();
	}

	public Node getVisual() {
		return text;
	}

	public int converseEdge(final int edge) {
		return getConverse(edge) + converseSpan();
	}

	public abstract int getConverse(int edge);
}
