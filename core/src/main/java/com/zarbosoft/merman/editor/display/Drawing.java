package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;

public interface Drawing extends DisplayNode {

	void clear();

	void resize(Context context, Vector vector);

	interface DrawingContext {

		void setLineColor(ModelColor color);

		void setLineCapRound();

		void setLineThickness(double lineThickness);

		void setLineCapFlat();

		void setFillColor(ModelColor color);

		void beginStrokePath();

		void beginFillPath();

		void moveTo(int c, int t);

		void lineTo(int c, int t);

		void closePath();

		void arcTo(int c, int t, int c2, int t2, int radius);

		void translate(int c, int t);
	}

	DrawingContext begin(Context context);

}
