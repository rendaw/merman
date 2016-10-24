package com.zarbosoft.bonestruct.visual.nodes;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.luxemj.Luxem;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Obbox extends Canvas {
	@Luxem.Configuration
	public static class Settings {
		@Luxem.Configuration(optional = true, name = "pad")
		public int padding = 0;
		@Luxem.Configuration(optional = true, name = "round-start")
		public boolean roundStart = false;
		@Luxem.Configuration(optional = true, name = "round-end")
		public boolean roundEnd = false;
		@Luxem.Configuration(optional = true, name = "round-outer-edges")
		public boolean roundOuterEdges = false;
		@Luxem.Configuration(optional = true, name = "round-inner-edges")
		public boolean roundInnerEdges = false;
		@Luxem.Configuration(optional = true, name = "round-concave")
		public boolean roundConcave = false;
		@Luxem.Configuration(optional = true, name = "round-radius")
		public int roundRadius = 5;
		@Luxem.Configuration(optional = true, name = "line")
		public boolean line = true;
		@Luxem.Configuration(optional = true, name = "line-color")
		public Color lineColor = Color.BLUEVIOLET;
		@Luxem.Configuration(optional = true, name = "line-thickness")
		public double lineThickness = 1;
		@Luxem.Configuration(optional = true, name = "fill")
		public boolean fill = false;
		@Luxem.Configuration(optional = true, name = "fill-color")
		public Color fillColor = Color.PAPAYAWHIP;
	}

	public static Obbox fromSettings(final Settings settings) {
		return new Obbox() {
			@Override
			public int padding() {
				return settings.padding;
			}

			@Override
			public int roundRadius() {
				return settings.roundRadius;
			}

			@Override
			public boolean roundStart() {
				return settings.roundStart;
			}

			@Override
			public boolean roundEnd() {
				return settings.roundEnd;
			}

			@Override
			public boolean roundOuterEdges() {
				return settings.roundOuterEdges;
			}

			@Override
			public boolean roundInnerEdges() {
				return settings.roundInnerEdges;
			}

			@Override
			public boolean roundConcave() {
				return settings.roundConcave;
			}

			@Override
			public boolean line() {
				return settings.line;
			}

			@Override
			public Color lineColor() {
				return settings.lineColor;
			}

			@Override
			public double lineThickness() {
				return settings.lineThickness;
			}

			@Override
			public boolean fill() {
				return settings.fill;
			}

			@Override
			public Color fillColor() {
				return settings.fillColor;
			}
		};
	}

	int radius = 0;

	public abstract int padding();

	public abstract int roundRadius();

	public abstract boolean roundStart();

	public abstract boolean roundEnd();

	public abstract boolean roundOuterEdges();

	public abstract boolean roundInnerEdges();

	public abstract boolean roundConcave();

	public abstract boolean line();

	public abstract Color lineColor();

	public abstract double lineThickness();

	public abstract boolean fill();

	public abstract Color fillColor();

	public static boolean isIn(
			final int sc, final int st, final int ste, final int ec, final int et, final int ete, final Vector point
	) {
		if (point.transverse < st)
			return false;
		if (point.transverse > ete)
			return false;
		if (point.transverse < ste && point.converse < sc)
			return false;
		if (point.transverse > et && point.converse > ec)
			return false;
		return true;
	}

	public void setSize(
			final Context context, int sc, int st, int ste, int ec, int et, int ete
	) {
		clear();
		sc -= padding();
		st -= padding();
		ste += padding();
		ec += padding();
		et -= padding();
		ete += padding();
		radius = roundRadius();
		//radius = Math.min(roundRadius(), Math.min(ste - st, ete - et));
		final int buffer = (int) (lineThickness() + 1);
		final Point2D wh =
				context.toScreenSpan(new Vector(context.edge + padding() * 2 + buffer * 2, ete - st + buffer * 2));
		setWidth(wh.getX());
		setHeight(wh.getY());
		context.translate(this, new Vector(-(buffer + padding()), st - buffer));
		final GraphicsContext gc = this.getGraphicsContext2D();
		gc.translate(buffer + padding(), buffer);
		ste -= st;
		et -= st;
		ete -= st;
		st = 0;
		if (fill()) {
			gc.setFill(fillColor());
			gc.beginPath();
			path(context, gc, -padding(), context.edge + padding(), sc, st, ste, ec, et, ete);
			gc.closePath();
			gc.fill();
		}
		if (line()) {
			gc.setStroke(lineColor());
			gc.setLineWidth(lineThickness());
			gc.beginPath();
			path(context, gc, -padding(), context.edge + padding(), sc, st, ste, ec, et, ete);
			gc.closePath();
			gc.stroke();
		}
	}

	private void clear() {
		final GraphicsContext gc = this.getGraphicsContext2D();
		gc.setFill(Color.TRANSPARENT);
		gc.fillRect(0, 0, getWidth(), getHeight());
	}

	private void path(
			final Context context,
			final GraphicsContext gc,
			final int converseZero,
			final int converseEdge,
			final int startConverse,
			final int startTransverse,
			final int startTransverseEdge,
			final int endConverse,
			final int endTransverse,
			final int endTransverseEdge
	) {
		if (startTransverse == endTransverse) {
			moveTo(context, gc, startConverse, (startTransverse + startTransverseEdge) / 2);
			cornerTo(
					context,
					gc,
					roundStart(),
					startConverse,
					startTransverse,
					(startConverse + endConverse) / 2,
					startTransverse
			);
			cornerTo(
					context,
					gc,
					roundOuterEdges(),
					endConverse,
					startTransverse,
					endConverse,
					(startTransverse + endTransverseEdge) / 2
			);
			cornerTo(
					context,
					gc,
					roundEnd(),
					endConverse,
					endTransverseEdge,
					(startConverse + endConverse) / 2,
					endTransverseEdge
			);
			cornerTo(
					context,
					gc,
					roundOuterEdges(),
					startConverse,
					endTransverseEdge,
					startConverse,
					(startTransverse + endTransverseEdge) / 2
			);
		} else {
			moveTo(context, gc, startConverse, (startTransverse + startTransverseEdge) / 2);
			cornerTo(
					context,
					gc,
					roundStart(),
					startConverse,
					startTransverse,
					(startConverse + converseEdge) / 2,
					startTransverse
			);
			cornerTo(
					context,
					gc,
					roundOuterEdges(),
					converseEdge,
					startTransverse,
					converseEdge,
					(startTransverse + endTransverse) / 2
			);
			cornerTo(
					context,
					gc,
					roundInnerEdges(),
					converseEdge,
					endTransverse,
					(endConverse + converseEdge) / 2,
					endTransverse
			);
			cornerTo(
					context,
					gc,
					roundConcave(),
					endConverse,
					endTransverse,
					endConverse,
					(endTransverse + endTransverseEdge) / 2
			);
			cornerTo(context, gc, roundEnd(), endConverse, endTransverseEdge, endConverse / 2, endTransverseEdge);
			cornerTo(
					context,
					gc,
					roundOuterEdges(),
					converseZero,
					endTransverseEdge,
					converseZero,
					(startTransverseEdge + endTransverseEdge) / 2
			);
			cornerTo(
					context,
					gc,
					roundInnerEdges(),
					converseZero,
					startTransverseEdge,
					startConverse / 2,
					startTransverseEdge
			);
			cornerTo(
					context,
					gc,
					roundConcave(),
					startConverse,
					startTransverseEdge,
					startConverse,
					(startTransverse + startTransverseEdge) / 2
			);
		}
	}

	private void moveTo(final Context context, final GraphicsContext gc, final int c, final int t) {
		final Point2D point = context.toScreen(new Vector(c, t));
		gc.moveTo(point.getX(), point.getY());
	}

	private void cornerTo(
			final Context context,
			final GraphicsContext gc,
			final boolean round,
			final int c,
			final int t,
			final int c2,
			final int t2
	) {
		final Point2D point = context.toScreen(new Vector(c, t));
		final Point2D point2 = context.toScreen(new Vector(c2, t2));
		if (round) {
			gc.arcTo(point.getX(), point.getY(), point2.getX(), point2.getY(), radius);
		} else {
			gc.lineTo(point.getX(), point.getY());
			gc.lineTo(point2.getX(), point2.getY());
		}
	}
}
