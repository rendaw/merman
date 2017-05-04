package com.zarbosoft.bonestruct.syntax.symbol;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.BrickInterface;
import com.zarbosoft.bonestruct.wall.bricks.BrickSpace;

public class SymbolSpace extends Symbol {
	@Override
	public DisplayNode createDisplay(final Context conspace) {
		return conspace.display.blank();
	}

	@Override
	public void style(final Context conspace, final DisplayNode node, final Style.Baked style) {
	}

	@Override
	public Brick createBrick(final Context conspace, final BrickInterface inter) {
		return new BrickSpace(conspace, inter);
	}
}
