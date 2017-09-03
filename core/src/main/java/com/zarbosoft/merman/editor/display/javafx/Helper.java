package com.zarbosoft.merman.editor.display.javafx;

import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Helper {

	public static Paint convert(final ModelColor color) {
		if (color instanceof ModelColor.RGB) {
			return Color.color(((ModelColor.RGB) color).r, ((ModelColor.RGB) color).g, ((ModelColor.RGB) color).b);
		} else if (color instanceof ModelColor.RGBA) {
			return Color.color(
					((ModelColor.RGBA) color).r,
					((ModelColor.RGBA) color).g,
					((ModelColor.RGBA) color).b,
					((ModelColor.RGBA) color).a
			);
		} else
			throw new DeadCode();
	}
}
