package com.zarbosoft.bonestruct.visual;

import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Helper;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Luxem.Configuration
public class Style {

	@Luxem.Configuration
	public Set<VisualNode.Tag> tags;

	@Luxem.Configuration(optional = true)
	public Obbox.Settings border = new Obbox.Settings();

	@Luxem.Configuration(name = "break", optional = true)
	public Boolean broken = null;

	@Luxem.Configuration(name = "align", optional = true)
	public String alignment = null;

	@Luxem.Configuration(name = "space-before", optional = true)
	public Integer spaceBefore = null;

	@Luxem.Configuration(name = "space-after", optional = true)
	public Integer spaceAfter = null;

	@Luxem.Configuration(name = "space-transverse-before", optional = true)
	public Integer spaceTransverseBefore = null;

	@Luxem.Configuration(name = "space-transverse-after", optional = true)
	public Integer spaceTransverseAfter = null;

	// Text/image/shape only

	@Luxem.Configuration(optional = true)
	public Color color = null;

	// Text only

	@Luxem.Configuration(optional = true)
	public String font = null;

	@Luxem.Configuration(name = "font-size", optional = true)
	public Integer fontSize = null;

	// Image only

	@Luxem.Configuration(optional = true)
	public String image = null;

	@Luxem.Configuration(optional = true)
	public Integer rotate = null;

	// Shape only

	@Luxem.Configuration(optional = true)
	public Integer converse = null;

	@Luxem.Configuration(optional = true)
	public Integer transverse = null;

	// Space only

	@Luxem.Configuration(optional = true)
	public Integer space = null;

	public static class Baked {
		public Set<VisualNode.Tag> tags = new HashSet<>();
		public boolean broken = false;
		public String alignment = null;
		public int spaceBefore = 0;
		public int spaceAfter = 0;
		public int spaceTransverseBefore = 0;
		public int spaceTransverseAfter = 0;
		public Color color = Color.BLACK;
		public String font = null;
		public int fontSize = 14;
		public String image = null;
		public int rotate = 0;
		public int converse = 12;
		public int transverse = 12;
		public int space = 0;

		public Baked(final Set<VisualNode.Tag> tags) {
			this.tags.addAll(tags);
		}

		public void merge(final Style style) {
			for (final Field field : Style.class.getFields()) {
				if (field.getAnnotation(Luxem.Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != Color.class)
					continue;
				final Object value = Helper.uncheck(() -> field.get(style));
				if (value != null)
					Helper.uncheck(() -> getClass().getField(field.getName()).set(this, value));
			}
		}

		public Font getFont() {
			if (font == null)
				return new Font(fontSize);
			return new Font(font, fontSize);
		}
	}
}
