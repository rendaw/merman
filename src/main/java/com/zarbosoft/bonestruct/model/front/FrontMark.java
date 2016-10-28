package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.nodes.parts.RawTextVisualPart;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;

@Luxem.Configuration(name = "mark")
public class FrontMark implements FrontConstantPart {

	@Luxem.Configuration
	public String value;

	@Luxem.Configuration
	public static class Style {
		@Luxem.Configuration(name = "break", optional = true)
		public VisualNodePart.Break breakMode = VisualNodePart.Break.NEVER;

		@Luxem.Configuration(name = "align", optional = true)
		public String alignment = null;

		@Luxem.Configuration(name = "align-compact", optional = true)
		public String alignmentCompact = null;
	}

	@Luxem.Configuration(optional = true)
	public Style style = new Style();

	@Override
	public VisualNodePart createVisual(final Context context) {
		final RawTextVisualPart out = new RawTextVisualPart(context) {

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				return null;
			}

			@Override
			public boolean select(final Context context) {
				return false;
			}

			@Override
			public Break breakMode() {
				return style.breakMode;
			}

			@Override
			public String alignmentName() {
				return style.alignment;
			}

			@Override
			public String alignmentNameCompact() {
				return style.alignmentCompact;
			}

		};
		out.setText(value);
		return out;
	}
}
