package com.zarbosoft.bonestruct.syntax.symbol;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Text;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.BrickInterface;
import com.zarbosoft.bonestruct.wall.bricks.BrickMark;

public class SymbolText extends Symbol {
	@Override
	public DisplayNode createDisplay(final Context context) {
		return context.display.text();
	}

	@Override
	public void style(final Context context, final DisplayNode node, final Style.Baked style) {
		final Text text = (Text) node;
		text.setFont(context, style.getFont(context));
		text.setColor(context, style.color);
	}

	@Override
	public Brick createBrick(final Context context, final BrickInterface inter) {
		return new BrickMark(context, inter);
	}
}
