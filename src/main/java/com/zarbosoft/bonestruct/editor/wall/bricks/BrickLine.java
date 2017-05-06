package com.zarbosoft.bonestruct.editor.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.bonestruct.syntax.style.Style;

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
		if (line.index > 0)
			style.broken = true;
		return super.properties(context, style);
	}

	@Override
	public Hoverable hover(final Context context, final Vector point) {
		return line.hover(context, point);
	}
}
