package com.zarbosoft.bonestruct.syntax.style;

import com.zarbosoft.interface1.Configuration;

import java.lang.reflect.Field;

import static com.zarbosoft.rendaw.common.Common.uncheck;

@Configuration
public class BoxStyle {
	@Configuration(optional = true, name = "pad")
	public Integer padding = null;
	@Configuration(optional = true, name = "round_start")
	public Boolean roundStart = null;
	@Configuration(optional = true, name = "round_end")
	public Boolean roundEnd = null;
	@Configuration(optional = true, name = "round_outer_edges")
	public Boolean roundOuterEdges = null;
	@Configuration(optional = true, name = "round_radius")
	public Integer roundRadius = null;
	@Configuration(optional = true, name = "line")
	public Boolean line = null;
	@Configuration(optional = true, name = "line_color")
	public ModelColor lineColor = null;
	@Configuration(optional = true, name = "line_thickness")
	public Double lineThickness = null;
	@Configuration(optional = true, name = "fill")
	public Boolean fill = null;
	@Configuration(optional = true, name = "fill_color")
	public ModelColor fillColor = null;

	public void merge(final BoxStyle settings) {
		for (final Field field : getClass().getFields()) {
			if (field.getAnnotation(Configuration.class) == null)
				continue;
			if (field.getType() != Integer.class &&
					field.getType() != Double.class &&
					field.getType() != Boolean.class &&
					field.getType() != String.class &&
					field.getType() != ModelColor.class)
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
		public int roundRadius = 0;
		public boolean line = true;
		public ModelColor lineColor = new ModelColor.RGB();
		public double lineThickness = 1;
		public boolean fill = false;
		public ModelColor fillColor = ModelColor.RGB.white;

		public void merge(final BoxStyle settings) {
			for (final Field field : BoxStyle.class.getFields()) {
				if (field.getAnnotation(Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != ModelColor.class)
					continue;
				final Object value = uncheck(() -> field.get(settings));
				if (value != null)
					uncheck(() -> getClass().getField(field.getName()).set(this, value));
			}
		}
	}
}
