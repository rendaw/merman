package com.zarbosoft.merman.editor.wall.bricks;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.syntax.style.Style;

public class BrickLine extends BrickText {
	private final VisualPrimitive.Line line;

	public BrickLine(
			final Context context, final VisualPrimitive.Line line
	) {
		super(context, line);
		this.line = line;
	}

	@Override
	public Properties properties(final Context context, final Style.Baked style) {
		final Font font = style.getFont(context);
		return new Properties(
				line.index == 0 ? style.split : true,
				font.getAscent(),
				font.getDescent(),
				inter.getAlignment(style),
				font.getWidth(text.text())
		);
	}

	@Override
	public Hoverable hover(final Context context, final Vector point) {
		return line.hover(context, point);
	}

	@Override
	public void setConverse(final Context context, final int minConverse, final int converse) {
		super.setConverse(context, minConverse, converse);
		line.idleResplit(context);
	}
}
