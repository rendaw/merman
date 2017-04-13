package com.zarbosoft.bonestruct.syntax.style;

import com.zarbosoft.interface1.Configuration;
import javafx.scene.paint.Color;

@Configuration
public abstract class ModelColor {
	public abstract Color get();

	@Configuration(name = "rgb")
	public static class RGB extends ModelColor {
		public final static ModelColor white;

		static {
			final RGB tempWhite = new RGB();
			tempWhite.r = 1;
			tempWhite.g = 1;
			tempWhite.b = 1;
			white = tempWhite;
		}

		@Configuration
		public double r;
		@Configuration
		public double g;
		@Configuration
		public double b;

		@Override
		public Color get() {
			return Color.color(r, g, b);
		}
	}
}
