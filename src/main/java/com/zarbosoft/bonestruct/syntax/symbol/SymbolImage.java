package com.zarbosoft.bonestruct.syntax.symbol;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Image;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.BrickInterface;
import com.zarbosoft.bonestruct.wall.bricks.BrickImage;
import com.zarbosoft.interface1.Configuration;

import java.nio.file.Paths;

@Configuration(name = "image")
public class SymbolImage extends Symbol {
	@Override
	public DisplayNode createDisplay(final Context context) {
		return context.display.image();
	}

	@Override
	public void style(final Context context, final DisplayNode node, final Style.Baked style) {
		final Image image = (Image) node;
		image.setImage(context, Paths.get(style.image));
		image.rotate(context, style.rotate);
	}

	@Override
	public Brick createBrick(final Context context, final BrickInterface inter) {
		return new BrickImage(context, inter);
	}
}
