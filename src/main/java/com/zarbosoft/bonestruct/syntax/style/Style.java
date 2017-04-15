package com.zarbosoft.bonestruct.syntax.style;

import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.interface1.Configuration;
import javafx.scene.text.Font;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.uncheck;

@Configuration
public class Style {

	@Configuration
	public Set<VisualNode.Tag> tags = new HashSet<>();

	@Configuration(optional = true)
	public ObboxStyle border = new ObboxStyle();

	@Configuration(name = "break", optional = true)
	public Boolean broken = null;

	@Configuration(name = "align", optional = true)
	public String alignment = null;

	@Configuration(name = "space_before", optional = true)
	public Integer spaceBefore = null;

	@Configuration(name = "space_after", optional = true)
	public Integer spaceAfter = null;

	@Configuration(name = "space_transverse_before", optional = true)
	public Integer spaceTransverseBefore = null;

	@Configuration(name = "space_transverse_after", optional = true)
	public Integer spaceTransverseAfter = null;

	// Text/image/shape only

	@Configuration(optional = true)
	public ModelColor color = null;

	// Text only

	@Configuration(optional = true)
	public String font = null;

	@Configuration(name = "font_size", optional = true)
	public Integer fontSize = null;

	// Image only

	@Configuration(optional = true)
	public String image = null;

	@Configuration(optional = true)
	public Integer rotate = null;

	// Shape only

	@Configuration(optional = true)
	public Integer converse = null;

	@Configuration(optional = true)
	public Integer transverse = null;

	// Space only

	@Configuration(optional = true)
	public Integer space = null;

	public static class Baked {
		public Set<VisualNode.Tag> tags = new HashSet<>();
		public boolean broken = false;
		public String alignment = null;
		public int spaceBefore = 0;
		public int spaceAfter = 0;
		public int spaceTransverseBefore = 0;
		public int spaceTransverseAfter = 0;
		public ModelColor color = new ModelColor.RGB();
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
				if (field.getAnnotation(Configuration.class) == null)
					continue;
				if (field.getType() != Integer.class &&
						field.getType() != Double.class &&
						field.getType() != Boolean.class &&
						field.getType() != String.class &&
						field.getType() != ModelColor.class)
					continue;
				final Object value = uncheck(() -> field.get(style));
				if (value != null)
					uncheck(() -> getClass().getField(field.getName()).set(this, value));
			}
		}

		public Font getFont() {
			if (font == null)
				return new Font(fontSize);
			return new Font(font, fontSize);
		}
	}
}
