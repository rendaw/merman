package com.zarbosoft.bonestruct.editor.visual.raw;

import com.zarbosoft.bonestruct.display.Drawing;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;

public abstract class Obbox {
	public final Drawing drawing;

	public Obbox(final Context context) {
		drawing = context.display.drawing();
	}

	public static Obbox fromSettings(final Context context, final ObboxStyle.Baked settings) {
		return new Obbox(context) {
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
			public ModelColor lineColor() {
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
			public ModelColor fillColor() {
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

	public abstract ModelColor lineColor();

	public abstract double lineThickness();

	public abstract boolean fill();

	public abstract ModelColor fillColor();

	public void setSize(
			final Context context, int sc, int st, int ste, int ec, int et, int ete
	) {
		final boolean oneLine = st == et;
		drawing.clear();
		sc -= padding();
		st -= padding();
		ste = oneLine ? ste + padding() : ste - padding();
		ec += padding();
		et += padding();
		ete += padding();
		radius = roundRadius();
		//radius = Math.min(roundRadius(), Math.min(ste - st, ete - et));
		final int buffer = (int) (lineThickness() + 1);
		final Vector wh = new Vector(context.edge + padding() * 2 + buffer * 2, ete - st + buffer * 2);
		drawing.resize(context, wh);
		drawing.setPosition(context, new Vector(-(buffer + padding()), st - buffer), false);
		final Drawing.DrawingContext gc = drawing.begin(context);
		gc.translate(buffer + padding(), buffer);
		ste -= st;
		et -= st;
		ete -= st;
		st = 0;
		if (fill()) {
			gc.setFillColor(fillColor());
			path(gc, oneLine, -padding(), context.edge + padding(), sc, st, ste, ec, et, ete);
			gc.fill();
		}
		if (line()) {
			gc.setLineColor(lineColor());
			gc.setLineThickness(lineThickness());
			path(gc, oneLine, -padding(), context.edge + padding(), sc, st, ste, ec, et, ete);
			gc.stroke();
		}
	}

	private void path(
			final Drawing.DrawingContext gc,
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
			moveTo(gc, startConverse, (startTransverse + startTransverseEdge) / 2);
			cornerTo(
					gc,
					roundStart(),
					startConverse,
					startTransverse,
					(startConverse + endConverse) / 2,
					startTransverse
			);
			cornerTo(
					gc,
					roundOuterEdges(),
					endConverse,
					startTransverse,
					endConverse,
					(startTransverse + startTransverseEdge) / 2
			);
			cornerTo(
					gc,
					roundEnd(),
					endConverse,
					startTransverseEdge,
					(startConverse + endConverse) / 2,
					startTransverseEdge
			);
			cornerTo(
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
			moveTo(gc, startConverse, (startTransverse + startTransverseEdge) / 2);
			cornerTo(
					gc,
					roundStart(),
					startConverse,
					startTransverse,
					(startConverse + converseEdge) / 2,
					startTransverse
			);
			cornerTo(
					gc,
					roundOuterEdges(),
					converseEdge,
					startTransverse,
					converseEdge,
					(startTransverse + endTransverse) / 2
			);
			if (endConverse == converseEdge) {
				cornerTo(
						gc,
						roundInnerEdges(),
						converseEdge,
						endTransverseEdge,
						(converseZero + converseEdge) / 2,
						endTransverseEdge
				);
			} else {
				cornerTo(
						gc,
						roundInnerEdges(),
						converseEdge,
						endTransverse,
						(endConverse + converseEdge) / 2,
						endTransverse
				);
				cornerTo(
						gc,
						roundConcave(),
						endConverse,
						endTransverse,
						endConverse,
						(endTransverse + endTransverseEdge) / 2
				);
				cornerTo(
						gc,
						roundEnd(),
						endConverse,
						endTransverseEdge,
						(converseZero + endConverse) / 2,
						endTransverseEdge
				);
			}
			if (startConverse == converseZero) {
				cornerTo(
						gc,
						roundOuterEdges(),
						converseZero,
						endTransverseEdge,
						converseZero,
						(startTransverse + startTransverseEdge) / 2
				);
			} else {
				cornerTo(
						gc,
						roundOuterEdges(),
						converseZero,
						endTransverseEdge,
						converseZero,
						(startTransverseEdge + endTransverseEdge) / 2
				);
				cornerTo(
						gc,
						roundInnerEdges(),
						converseZero,
						startTransverseEdge,
						startConverse / 2,
						startTransverseEdge
				);
				cornerTo(
						gc,
						roundConcave(),
						startConverse,
						startTransverseEdge,
						startConverse,
						(startTransverse + startTransverseEdge) / 2
				);
			}
			gc.closePath();
		}
	}

	private void moveTo(final Drawing.DrawingContext gc, final int c, final int t) {
		gc.moveTo(c, t);
	}

	private void cornerTo(
			final Drawing.DrawingContext gc, final boolean round, final int c, final int t, final int c2, final int t2
	) {
		if (round) {
			gc.arcTo(c, t, c2, t2, radius);
		} else {
			gc.lineTo(c, t);
			gc.lineTo(c2, t2);
		}
	}

}
