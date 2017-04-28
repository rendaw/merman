package com.zarbosoft.bonestruct.display;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;

public interface Drawing extends DisplayNode {

	void clear();

	void resize(Context context, Vector vector);

	interface DrawingContext {

		void setLineColor(ModelColor color);

		void setLineCapRound();

		void setLineThickness(double lineThickness);

		void setLineCapFlat();

		void setFillColor(ModelColor color);

		void beginPath();

		void moveTo(int halfBuffer, int halfBuffer1);

		void lineTo(int i, int i1);

		void closePath();

		void stroke();

		void fill();

		void arcTo(int c, int t, int c2, int t2, int radius);

		void translate(int c, int t);
	}

	DrawingContext begin(Context context);

}
