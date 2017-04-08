package com.zarbosoft.bonestruct.syntax.style;

import com.zarbosoft.interface1.Configuration;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;

import static com.zarbosoft.rendaw.common.Common.uncheck;

@Configuration
public class ObboxStyle {
	@Configuration(optional = true, name = "pad")
	public Integer padding = null;
	@Configuration(optional = true, name = "round-start")
	public Boolean roundStart = null;
	@Configuration(optional = true, name = "round-end")
	public Boolean roundEnd = null;
	@Configuration(optional = true, name = "round-outer-edges")
	public Boolean roundOuterEdges = null;
	@Configuration(optional = true, name = "round-inner-edges")
	public Boolean roundInnerEdges = null;
	@Configuration(optional = true, name = "round-concave")
	public Boolean roundConcave = null;
	@Configuration(optional = true, name = "round-radius")
	public Integer roundRadius = null;
	@Configuration(optional = true, name = "line")
	public Boolean line = null;
	@Configuration(optional = true, name = "line-color")
	public Color lineColor = null;
	@Configuration(optional = true, name = "line-thickness")
	public Double lineThickness = null;
	@Configuration(optional = true, name = "fill")
	public Boolean fill = null;
	@Configuration(optional = true, name = "fill-color")
	public Color fillColor = null;

	public void merge(final ObboxStyle settings) {
		for (final Field field : getClass().getFields()) {
			if (field.getAnnotation(Configuration.class) == null)
				continue;
			if (field.getType() != Integer.class &&
					field.getType() != Double.class &&
					field.getType() != Boolean.class &&
					field.getType() != String.class &&
					field.getType() != Color.class)
				continue;
			final Object value = uncheck(() -> field.get(settings));
			if (value != null)
				uncheck(() -> field.set(this, value));
		}
	}

	public static class Baked {
		public int padding = 4;
		public boolean roundStart = false;
		public boolean roundEnd = false;
		public boolean roundOuterEdges = false;
		public boolean roundInnerEdges = false;
		public boolean roundConcave = false;
		public int roundRadius = 0;
		public boolean line = true;
		public Color lineColor = Color.BLACK;
		public double lineThickness = 1;
		public boolean fill = false;
		public Color fillColor = Color.WHITE;

		public void merge(final ObboxStyle settings) {
			for (final Field field : ObboxStyle.class.getFields()) {
				if (field.getAnnotation(Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != Color.class)
					continue;
				final Object value = uncheck(() -> field.get(settings));
				if (value != null)
					uncheck(() -> getClass().getField(field.getName()).set(this, value));
			}
		}
	}
}
