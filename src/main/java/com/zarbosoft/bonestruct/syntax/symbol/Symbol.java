package com.zarbosoft.bonestruct.syntax.symbol;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.BrickInterface;
import com.zarbosoft.interface1.Configuration;

@Configuration
public abstract class Symbol {
	public abstract DisplayNode createDisplay(Context context);

	public abstract void style(Context context, DisplayNode node, Style.Baked style);

	public abstract Brick createBrick(Context context, BrickInterface inter);
}
