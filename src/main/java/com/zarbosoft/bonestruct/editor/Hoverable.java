package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeType;
import com.zarbosoft.bonestruct.syntax.style.Style;

public abstract class Hoverable {
	protected abstract void clear(Context context);

	public abstract void click(Context context);

	public abstract VisualNodeType node();

	public abstract VisualPart part();

	public abstract void globalTagsChanged(Context context);

	public Style.Baked getStyle(final Context context) {
		return context.getStyle(context.globalTags.plus(new Visual.PartTag("hover")));
	}

}
