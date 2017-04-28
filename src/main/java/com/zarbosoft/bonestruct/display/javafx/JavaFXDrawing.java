package com.zarbosoft.bonestruct.display.javafx;

import com.zarbosoft.bonestruct.display.Drawing;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.transform.Affine;

public class JavaFXDrawing extends JavaFXNode implements Drawing {
	public Canvas node;

	@Override
	protected Node node() {
		return node;
	}

	public Point2D toScreen(final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector source) {
		double x = 0, y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				y = context.display.edge(context) - source.converse;
				break;
			case DOWN:
				y = source.converse;
				break;
			case LEFT:
				x = context.display.edge(context) - source.converse;
				break;
			case RIGHT:
				x = source.converse;
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				y = context.display.transverseEdge(context) - source.transverse;
				break;
			case DOWN:
				y = source.transverse;
				break;
			case LEFT:
				x = context.display.transverseEdge(context) - source.transverse;
				break;
			case RIGHT:
				x = source.transverse;
				break;
		}
		return new Point2D(x, y);
	}

	public Point2D toScreenSpan(final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector source) {
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
	}

	@Override
	public DrawingContext begin(final Context context) {
		final GraphicsContext gc = node.getGraphicsContext2D();
		return new DrawingContext() {
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
			public void beginPath() {
				gc.beginPath();
			}

			@Override
			public void moveTo(final int converse, final int transverse) {
				final Point2D point = toScreen(context, new Vector(converse, transverse));
				gc.moveTo(point.getX(), point.getY());
			}

			@Override
			public void lineTo(final int converse, final int transverse) {
				final Point2D point = toScreen(context, new Vector(converse, transverse));
				gc.lineTo(point.getX(), point.getY());
			}

			@Override
			public void closePath() {
				gc.closePath();
			}

			@Override
			public void stroke() {
				gc.stroke();
			}

			@Override
			public void fill() {
				gc.fill();
			}

			@Override
			public void arcTo(final int c, final int t, final int c2, final int t2, final int radius) {
				final Point2D point1 = toScreen(context, new Vector(c, t));
				final Point2D point2 = toScreen(context, new Vector(c2, t2));
				gc.arcTo(point1.getX(), point1.getY(), point2.getX(), point2.getY(), radius);
			}

			@Override
			public void translate(final int c, final int t) {
				final Point2D point = toScreen(context, new Vector(c, t));
				gc.translate(point.getX(), point.getY());
			}
		};
	}
}
