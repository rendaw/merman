package com.zarbosoft.merman.editor.display.javafx;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;

public class JavaFXDrawing extends JavaFXNode implements Drawing {
	final public Canvas node;

	public JavaFXDrawing() {
		this.node = new Canvas();
	}

	@Override
	protected Node node() {
		return node;
	}

	public Point2D toScreen(final Context context, final Vector source, final boolean stroke) {
		double x = 0, y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				y = -(source.converse + (stroke ? 0.5 : 0));
				break;
			case DOWN:
				y = source.converse + (stroke ? 0.5 : 0);
				break;
			case LEFT:
				x = -(source.converse + (stroke ? 0.5 : 0));
				break;
			case RIGHT:
				x = source.converse + (stroke ? 0.5 : 0);
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				y = -(source.transverse + (stroke ? 0.5 : 0));
				break;
			case DOWN:
				y = source.transverse + (stroke ? 0.5 : 0);
				break;
			case LEFT:
				x = -(source.transverse + (stroke ? 0.5 : 0));
				break;
			case RIGHT:
				x = source.transverse + (stroke ? 0.5 : 0);
				break;
		}
		return new Point2D(x, y);
	}

	public Point2D toScreenSpan(final Context context, final Vector source) {
		double x = 0, y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				x = source.transverse;
				y = source.converse;
				break;
			case LEFT:
			case RIGHT:
				x = source.converse;
				y = source.transverse;
				break;
		}
		return new Point2D(x, y);
	}

	@Override
	public void clear() {
		final GraphicsContext gc = node.getGraphicsContext2D();
		gc.setTransform(new Affine());
		gc.clearRect(0, 0, node.getWidth(), node.getHeight());
	}

	@Override
	public void resize(final Context context, final Vector vector) {
		final Point2D size = toScreenSpan(context, vector);
		node.setWidth(size.getX());
		node.setHeight(size.getY());
		properties = null;
	}

	@Override
	public DrawingContext begin(final Context context) {
		final GraphicsContext gc = node.getGraphicsContext2D();
		return new DrawingContext() {
			Boolean stroke = null;

			@Override
			public void setLineColor(final ModelColor color) {
				gc.setStroke(Helper.convert(color));
			}

			@Override
			public void setLineCapRound() {
				gc.setLineCap(StrokeLineCap.ROUND);
			}

			@Override
			public void setLineThickness(final double lineThickness) {
				gc.setLineWidth(lineThickness);
			}

			@Override
			public void setLineCapFlat() {
				gc.setLineCap(StrokeLineCap.BUTT);
			}

			@Override
			public void setFillColor(final ModelColor color) {
				gc.setFill(Helper.convert(color));
			}

			@Override
			public void moveTo(final int converse, final int transverse) {
				final Point2D point = toScreen(context, new Vector(converse, transverse), stroke);
				gc.moveTo(point.getX(), point.getY());
			}

			@Override
			public void lineTo(final int converse, final int transverse) {
				final Point2D point = toScreen(context, new Vector(converse, transverse), stroke);
				gc.lineTo(point.getX(), point.getY());
			}

			@Override
			public void beginStrokePath() {
				stroke = true;
				gc.beginPath();
			}

			@Override
			public void beginFillPath() {
				stroke = false;
				gc.beginPath();
			}

			@Override
			public void closePath() {
				gc.closePath();
				if (stroke == null)
					throw new AssertionError();
				if (stroke)
					gc.stroke();
				else
					gc.fill();
				stroke = null;
			}

			@Override
			public void arcTo(final int c, final int t, final int c2, final int t2, final int radius) {
				final Point2D point1 = toScreen(context, new Vector(c, t), stroke);
				final Point2D point2 = toScreen(context, new Vector(c2, t2), stroke);
				gc.arcTo(point1.getX(), point1.getY(), point2.getX(), point2.getY(), radius);
			}

			@Override
			public void translate(final int c, final int t) {
				final Point2D point = toScreen(context, new Vector(c, t), false);
				gc.translate(point.getX(), point.getY());
			}
		};
	}
}
