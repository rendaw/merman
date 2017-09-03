package com.zarbosoft.merman.syntax.symbol;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.editor.wall.bricks.BrickSpace;
import com.zarbosoft.merman.syntax.style.Style;

@Configuration(name = "space")
public class SymbolSpace extends Symbol {
	@Override
	public DisplayNode createDisplay(final Context context) {
		final Blank blank = context.display.blank();
		return blank;
	}

	@Override
	public void style(final Context context, final DisplayNode node, final Style.Baked style) {
	}

	@Override
	public Brick createBrick(final Context context, final BrickInterface inter) {
		return new BrickSpace(context, inter);
	}
}
