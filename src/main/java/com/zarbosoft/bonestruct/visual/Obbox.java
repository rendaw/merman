package com.zarbosoft.bonestruct.visual;

import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Helper;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

import java.lang.reflect.Field;

public abstract class Obbox extends Canvas {
	@Luxem.Configuration
	public static class Settings {
		@Luxem.Configuration(optional = true, name = "pad")
		public Integer padding = null;
		@Luxem.Configuration(optional = true, name = "round-start")
		public Boolean roundStart = null;
		@Luxem.Configuration(optional = true, name = "round-end")
		public Boolean roundEnd = null;
		@Luxem.Configuration(optional = true, name = "round-outer-edges")
		public Boolean roundOuterEdges = null;
		@Luxem.Configuration(optional = true, name = "round-inner-edges")
		public Boolean roundInnerEdges = null;
		@Luxem.Configuration(optional = true, name = "round-concave")
		public Boolean roundConcave = null;
		@Luxem.Configuration(optional = true, name = "round-radius")
		public Integer roundRadius = null;
		@Luxem.Configuration(optional = true, name = "line")
		public Boolean line = null;
		@Luxem.Configuration(optional = true, name = "line-color")
		public Color lineColor = null;
		@Luxem.Configuration(optional = true, name = "line-thickness")
		public Double lineThickness = null;
		@Luxem.Configuration(optional = true, name = "fill")
		public Boolean fill = null;
		@Luxem.Configuration(optional = true, name = "fill-color")
		public Color fillColor = null;

		public void merge(final Settings settings) {
			for (final Field field : getClass().getFields()) {
				if (field.getAnnotation(Luxem.Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != Color.class)
					continue;
				final Object value = Helper.uncheck(() -> field.get(settings));
				if (value != null)
					Helper.uncheck(() -> field.set(this, value));
			}
		}
	}

	public static Obbox fromSettings(final BakedSettings settings) {
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

	public void setSize(
			final Context context, int sc, int st, int ste, int ec, int et, int ete
	) {
		System.out.format("te.setSize(context, %d, %d, %d, %d, %d, %d);\n", sc, st, ste, ec, et, ete);
		final boolean oneLine = st == et;
		clear();
		sc -= padding();
		st -= padding();
		ste = oneLine ? ste + padding() : ste - padding();
		ec += padding();
		et += padding();
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
			path(context, gc, oneLine, -padding(), context.edge + padding(), sc, st, ste, ec, et, ete);
			gc.fill();
		}
		if (line()) {
			gc.setStroke(lineColor());
			gc.setLineWidth(lineThickness());
			path(context, gc, oneLine, -padding(), context.edge + padding(), sc, st, ste, ec, et, ete);
			gc.stroke();
		}
	}

	private void clear() {
		final GraphicsContext gc = this.getGraphicsContext2D();
		gc.setTransform(new Affine());
		gc.clearRect(0, 0, getWidth(), getHeight());
	}

	private void path(
			final Context context,
			final GraphicsContext gc,
			final boolean oneLine,
			final int converseZero,
			final int converseEdge,
			final int startConverse,
			final int startTransverse,
			final int startTransverseEdge,
			final int endConverse,
			final int endTransverse,
			final int endTransverseEdge
	) {
		if (oneLine) {
			gc.beginPath();
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
					(startTransverse + startTransverseEdge) / 2
			);
			cornerTo(
					context,
					gc,
					roundEnd(),
					endConverse,
					startTransverseEdge,
					(startConverse + endConverse) / 2,
					startTransverseEdge
			);
			cornerTo(
					context,
					gc,
					roundOuterEdges(),
					startConverse,
					startTransverseEdge,
					startConverse,
					(startTransverse + startTransverseEdge) / 2
			);
			gc.closePath();
		} else {
			gc.beginPath();
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
			cornerTo(
					context,
					gc,
					roundEnd(),
					endConverse,
					endTransverseEdge,
					(converseZero + endConverse) / 2,
					endTransverseEdge
			);
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
			gc.closePath();
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

	public static class BakedSettings {
		public int padding = 0;
		public boolean roundStart = false;
		public boolean roundEnd = false;
		public boolean roundOuterEdges = false;
		public boolean roundInnerEdges = false;
		public boolean roundConcave = false;
		public int roundRadius = 0;
		public boolean line = true;
		public Color lineColor = Color.BLACK;
		public double lineThickness = 1;
		public boolean fill = false;
		public Color fillColor = Color.WHITE;

		public void merge(final Settings settings) {
			for (final Field field : Settings.class.getFields()) {
				if (field.getAnnotation(Luxem.Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != Color.class)
					continue;
				final Object value = Helper.uncheck(() -> field.get(settings));
				if (value != null)
					Helper.uncheck(() -> getClass().getField(field.getName()).set(this, value));
			}
		}
	}
}
