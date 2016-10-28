package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public abstract class RawText extends Text {

	public RawText(final Context context) {
		setTextOrigin(VPos.CENTER);
		if (context.syntax.fontSize != null) {
			if (context.syntax.font != null) {
				setFont(Font.font(context.syntax.font, context.syntax.fontSize));
			} else
				setFont(Font.font(context.syntax.fontSize));

		}
		setFill(context.syntax.fontColor);
	}

	public abstract Vector edge();

	public static RawText create(final Context context) {
		if (context.syntax.converseDirection == Syntax.Direction.LEFT ||
				context.syntax.converseDirection == Syntax.Direction.RIGHT)
			return new HorizontalRawText(context);
		else
			return new VerticalRawText(context);
	}

	public abstract int getUnder(int edge);

	private static class HorizontalRawText extends RawText {
		public HorizontalRawText(final Context context) {
			super(context);
		}

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
		public VerticalRawText(final Context context) {
			super(context);
		}

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
