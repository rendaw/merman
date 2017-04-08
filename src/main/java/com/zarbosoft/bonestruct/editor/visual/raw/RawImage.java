package com.zarbosoft.bonestruct.editor.visual.raw;

import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.syntax.style.Style;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class RawImage {
	protected final ImageView view = new ImageView();

	public RawImage(final Context context, final Style.Baked style) {
		setStyle(style);
	}

	public void setStyle(final Style.Baked style) {
		setImage(style.image);
		view.setRotate(style.rotate);
	}

	public static RawImage create(final Context context, final Style.Baked style) {
		switch (context.syntax.converseDirection) {
			case UP:
				switch (context.syntax.transverseDirection) {
					case UP:
					case DOWN:
						throw new AssertionError("dead code");
					case LEFT:
						return new RawImage(context, style) {
							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutY(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) view.getLayoutY();
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutX(edge - transverse);
							}

							@Override
							public void setImage(final String path) {
								final int oldSpan = converseSpan();
								view.setImage(new Image(path));
								view.setLayoutY(view.getLayoutY() + oldSpan - converseSpan());
							}
						};
					case RIGHT:
						return new RawImage(context, style) {

							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutY(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) view.getLayoutY();
							}

							public void setImage(final String path) {
								final int oldConverseSpan = converseSpan();
								final int oldTransverseSpan = transverseSpan();
								view.setImage(new Image(path));
								view.setLayoutY(view.getLayoutY() + oldConverseSpan - converseSpan());
								view.setLayoutX(view.getLayoutX() + oldTransverseSpan - transverseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutX(transverse - transverseSpan());
							}
						};
				}
			case DOWN:
				switch (context.syntax.transverseDirection) {
					case UP:
					case DOWN:
						throw new AssertionError("dead code");
					case LEFT:
						return new RawImage(context, style) {

							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutY(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) view.getLayoutY();
							}

							public void setImage(final String path) {
								view.setImage(new Image(path));
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutX(edge - transverse);
							}
						};
					case RIGHT:
						return new RawImage(context, style) {

							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutY(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) view.getLayoutY();
							}

							public void setImage(final String path) {
								final int oldSpan = transverseSpan();
								view.setImage(new Image(path));
								view.setLayoutX(view.getLayoutY() + oldSpan - transverseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutX(transverse - transverseSpan());
							}
						};
				}
			case LEFT:
				switch (context.syntax.transverseDirection) {
					case UP:
						return new RawImage(context, style) {

							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutX(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) view.getLayoutX();
							}

							public void setImage(final String path) {
								final int oldSpan = converseSpan();
								view.setImage(new Image(path));
								view.setLayoutX(view.getLayoutX() + oldSpan - converseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutY(edge - transverse);
							}
						};
					case DOWN:
						return new RawImage(context, style) {

							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutX(edge - converse - converseSpan());
							}

							@Override
							public int getConverse(final int edge) {
								return edge - converseSpan() - (int) view.getLayoutX();
							}

							public void setImage(final String path) {
								final int oldConverseSpan = converseSpan();
								final int oldTransverseSpan = transverseSpan();
								view.setImage(new Image(path));
								view.setLayoutX(view.getLayoutX() + oldConverseSpan - converseSpan());
								view.setLayoutY(view.getLayoutY() + oldTransverseSpan - transverseSpan());
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutY(transverse - transverseSpan());
							}
						};
					case LEFT:
					case RIGHT:
						throw new AssertionError("dead code");
				}
			case RIGHT:
				switch (context.syntax.transverseDirection) {
					case UP:
						return new RawImage(context, style) {
							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutX(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) view.getLayoutX();
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutY(edge - transverse);
							}

							@Override
							public void setImage(final String path) {
								view.setImage(new Image(path));
							}
						};
					case DOWN:
						return new RawImage(context, style) {
							@Override
							public int converseSpan() {
								return (int) view.getLayoutBounds().getWidth();
							}

							@Override
							public void setConverse(final int converse, final int edge) {
								view.setLayoutX(converse);
							}

							@Override
							public int getConverse(final int edge) {
								return (int) view.getLayoutX();
							}

							@Override
							public int transverseSpan() {
								return (int) view.getLayoutBounds().getHeight();
							}

							@Override
							public void setTransverse(final int transverse, final int edge) {
								view.setLayoutY(transverse - transverseSpan());
							}

							@Override
							public void setImage(final String path) {
								final int oldSpan = transverseSpan();
								view.setImage(new Image(path));
								view.setLayoutY(view.getLayoutY() + oldSpan - transverseSpan());
							}
						};
					case LEFT:
					case RIGHT:
						throw new AssertionError("dead code");
				}
		}
		throw new AssertionError("dead code");
	}

	public abstract int converseSpan();

	public abstract void setConverse(int converse, int edge);

	public abstract int transverseSpan();

	public abstract void setTransverse(int transverse, int edge);

	protected abstract void setImage(final String path);

	public Node getVisual() {
		return view;
	}

	public int converseEdge(final int edge) {
		return getConverse(edge) + converseSpan();
	}

	public abstract int getConverse(int edge);
}
