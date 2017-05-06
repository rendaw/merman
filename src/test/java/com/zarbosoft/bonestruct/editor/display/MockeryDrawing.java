package com.zarbosoft.bonestruct.editor.display;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;

public class MockeryDrawing extends MockeryDisplayNode implements Drawing {
	private Vector size = new Vector(0, 0);

	@Override
	public int converseSpan(final Context context) {
		return size.converse;
	}

	@Override
	public int transverseSpan(final Context context) {
		return size.transverse;
	}

	@Override
	public void clear() {

	}

	@Override
	public void resize(final Context context, final Vector vector) {
		MockeryDrawing.this.size = vector;
	}

	@Override
	public DrawingContext begin(final Context context) {
		return new DrawingContext() {
			@Override
			public void setLineColor(final ModelColor color) {

			}

			@Override
			public void setLineCapRound() {

			}

			@Override
			public void setLineThickness(final double lineThickness) {

			}

			@Override
			public void setLineCapFlat() {

			}

			@Override
			public void setFillColor(final ModelColor color) {

			}

			@Override
			public void beginPath() {

			}

			@Override
			public void moveTo(final int halfBuffer, final int halfBuffer1) {

			}

			@Override
			public void lineTo(final int i, final int i1) {

			}

			@Override
			public void closePath() {

			}

			@Override
			public void stroke() {

			}

			@Override
			public void fill() {

			}

			@Override
			public void arcTo(final int c, final int t, final int c2, final int t2, final int radius) {

			}

			@Override
			public void translate(final int c, final int t) {

			}
		};
	}
}
