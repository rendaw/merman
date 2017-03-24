package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.interface1.Configuration;
import javafx.scene.paint.Color;

@Configuration
public abstract class ModelColor {
	public abstract Color get();

	@Configuration(name = "rgb")
	public static class RGB extends ModelColor {
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
