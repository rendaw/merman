package com.zarbosoft.bonestruct.editor.wall.bricks;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.syntax.style.Style;

public class BrickMark extends BrickText {
	private Alignment alignment;

	public BrickMark(final Context context, final BrickInterface inter) {
		super(context, inter);
	}

	@Override
	public void setStyle(final Context context, final Style.Baked style) {
		if (alignment != null)
			alignment.removeListener(context, this);
		alignment = inter.getAlignment(style);
		if (alignment != null)
			alignment.addListener(context, this);
		changed(context);
		super.setStyle(context, style);
	}

	@Override
	public void destroyed(final Context context) {
		super.destroyed(context);
		if (alignment != null)
			alignment.removeListener(context, this);
	}
}
