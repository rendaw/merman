package com.zarbosoft.merman.editor.display.derived;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Drawing;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.BoxStyle;

public class Box {
	public final Drawing drawing;
	private Vector offset;
	private BoxStyle.Baked style;

	public Box(final Context context) {
		drawing = context.display.drawing();
	}

	public void setStyle(final Context context, final BoxStyle.Baked style) {
		this.style = style;
	}

	int radius = 0;

	public void setPosition(final Context context, final Vector vector, final boolean animate) {
		drawing.setPosition(context, offset.add(vector), animate);
	}

	public void setSize(
			final Context context, int converseSpan, int transverseSpan
	) {
		drawing.clear();
		converseSpan += style.padding * 2;
		transverseSpan += style.padding * 2;
		radius = style.roundRadius;
		final int buffer = (int) (style.lineThickness + 1);
		drawing.resize(context, new Vector(converseSpan + buffer * 2, transverseSpan + buffer * 2));
		offset = new Vector(-(buffer + style.padding), -(buffer + style.padding));
		final Drawing.DrawingContext gc = drawing.begin(context);
		gc.translate(buffer, buffer);
		if (style.fill) {
			gc.beginFillPath();
			gc.setFillColor(style.fillColor);
			path(gc, converseSpan, transverseSpan);
			gc.closePath();
		}
		if (style.line) {
			gc.beginStrokePath();
			gc.setLineColor(style.lineColor);
			gc.setLineThickness(style.lineThickness);
			path(gc, converseSpan, transverseSpan);
			gc.closePath();
		}
	}

	private void path(
			final Drawing.DrawingContext gc, final int converseSpan, final int transverseSpan
	) {
		moveTo(gc, 0, transverseSpan / 2);
		cornerTo(gc, style.roundStart, 0, 0, converseSpan / 2, 0);
		cornerTo(gc, style.roundOuterEdges, converseSpan, 0, converseSpan, transverseSpan / 2);
		cornerTo(gc, style.roundEnd, converseSpan, transverseSpan, converseSpan / 2, transverseSpan);
		cornerTo(gc, style.roundOuterEdges, 0, transverseSpan, 0, transverseSpan / 2);
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
