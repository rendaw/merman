package com.zarbosoft.merman.syntax.style;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.visual.tags.Tag;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.uncheck;

@Configuration
public class Style {

	@Configuration()
	public Set<Tag> with = new HashSet<>();
	@Configuration(optional = true)
	public Set<Tag> without = new HashSet<>();

	@Configuration(name = "split", optional = true)
	public Boolean split = null;

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

	// Other

	@Configuration(optional = true)
	public BoxStyle box = null;

	@Configuration(optional = true)
	public ObboxStyle obbox = null;

	public static class Baked {
		public Set<Tag> tags = new HashSet<>();
		public boolean split = false;
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
		public BoxStyle.Baked box = new BoxStyle.Baked();
		public ObboxStyle.Baked obbox = new ObboxStyle.Baked();

		public Baked(final Set<Tag> tags) {
			this.tags.addAll(tags);
		}

		public static Set<Class<?>> mergeableTypes = ImmutableSet.of(
				Integer.class,
				Double.class,
				Boolean.class,
				String.class,
				ModelColor.class,
				BoxStyle.class,
				ObboxStyle.class
		);

		public void merge(final Style style) {
			for (final Field field : Style.class.getFields()) {
				if (field.getAnnotation(Configuration.class) == null)
					continue;
				if (!mergeableTypes.contains(field.getType()))
					continue;
				final Object value = uncheck(() -> field.get(style));
				if (value != null) {
					if (field.getName().equals("box"))
						box.merge((BoxStyle) value);
					else if (field.getName().equals("obbox"))
						obbox.merge((ObboxStyle) value);
					else
						uncheck(() -> getClass().getField(field.getName()).set(this, value));
				}
			}
		}

		public Font getFont(final Context context) {
			if (font == null)
				return context.display.font(null, fontSize);
			return context.display.font(font, fontSize);
		}
	}
}
