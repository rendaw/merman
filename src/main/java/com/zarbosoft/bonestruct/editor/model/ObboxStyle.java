package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Helper;
import javafx.scene.paint.Color;

import java.lang.reflect.Field;

@Luxem.Configuration
public class ObboxStyle {
	@Luxem.Configuration(optional = true, name = "pad")
	public Integer padding = null;
	@Luxem.Configuration(optional = true, name = "round-start")
	public Boolean roundStart = null;
	@Luxem.Configuration(optional = true, name = "round-end")
	public Boolean roundEnd = null;
	@Luxem.Configuration(optional = true, name = "round-outer-edges")
	public Boolean roundOuterEdges = null;
	@Luxem.Configuration(optional = true, name = "round-inner-edges")
	public Boolean roundInnerEdges = null;
	@Luxem.Configuration(optional = true, name = "round-concave")
	public Boolean roundConcave = null;
	@Luxem.Configuration(optional = true, name = "round-radius")
	public Integer roundRadius = null;
	@Luxem.Configuration(optional = true, name = "line")
	public Boolean line = null;
	@Luxem.Configuration(optional = true, name = "line-color")
	public Color lineColor = null;
	@Luxem.Configuration(optional = true, name = "line-thickness")
	public Double lineThickness = null;
	@Luxem.Configuration(optional = true, name = "fill")
	public Boolean fill = null;
	@Luxem.Configuration(optional = true, name = "fill-color")
	public Color fillColor = null;

	public void merge(final ObboxStyle settings) {
		for (final Field field : getClass().getFields()) {
			if (field.getAnnotation(Luxem.Configuration.class) == null)
				continue;
			if (field.getType() != Integer.class &&
					field.getType() != Double.class &&
					field.getType() != Boolean.class &&
					field.getType() != String.class &&
					field.getType() != Color.class)
				continue;
			final Object value = Helper.uncheck(() -> field.get(settings));
			if (value != null)
				Helper.uncheck(() -> field.set(this, value));
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
				if (field.getAnnotation(Luxem.Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != Color.class)
					continue;
				final Object value = Helper.uncheck(() -> field.get(settings));
				if (value != null)
					Helper.uncheck(() -> getClass().getField(field.getName()).set(this, value));
			}
		}
	}
}
