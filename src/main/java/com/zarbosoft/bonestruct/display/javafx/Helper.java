package com.zarbosoft.bonestruct.display.javafx;

import com.zarbosoft.bonestruct.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class Helper {

	public static Paint convert(final ModelColor color) {
		if (color instanceof ModelColor.RGB) {
			return Color.color(((ModelColor.RGB) color).r, ((ModelColor.RGB) color).g, ((ModelColor.RGB) color).b);
		} else
			throw new DeadCode();
	}
}
