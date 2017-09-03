package com.zarbosoft.merman.editor;

import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.syntax.style.Style;
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
