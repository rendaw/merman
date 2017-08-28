package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.syntax.style.Style;
import org.pcollections.PSet;

public abstract class Hoverable {
	protected abstract void clear(Context context);

	public abstract void click(Context context);

	public abstract VisualAtom atom();

	public abstract Visual visual();

	public abstract void tagsChanged(
			Context context
	);

	public Style.Baked getBorderStyle(final Context context, final PSet<Tag> tags) {
		return context.getStyle(context.globalTags.plusAll(tags).plus(new PartTag("hover")));
	}

}
