package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.text.Text;

public abstract class RawText extends Text {

	public RawText() {
		setTextOrigin(VPos.TOP);
	}

	public abstract Vector edge();

	public static RawText create(final Context context) {
		if (context.syntax.converseDirection == Syntax.Direction.LEFT ||
				context.syntax.converseDirection == Syntax.Direction.RIGHT)
			return new HorizontalRawText();
		else
			return new VerticalRawText();
	}

	public abstract int getUnder(int edge);

	private static class HorizontalRawText extends RawText {
		@Override
		public Vector edge() {
			final Bounds bounds = getLayoutBounds();
			return new Vector((int) bounds.getWidth(), (int) bounds.getHeight());
		}

		@Override
		public int getUnder(final int edge) {
			return impl_hitTestChar(new Point2D((double) edge, getY())).getCharIndex();
		}
	}

	private static class VerticalRawText extends RawText {
		@Override
		public Vector edge() {
			final Bounds bounds = getLayoutBounds();
			return new Vector((int) bounds.getHeight(), (int) bounds.getWidth());
		}

		@Override
		public int getUnder(final int edge) {
			return impl_hitTestChar(new Point2D(getX(), (double) edge)).getCharIndex();
		}
	}
}
