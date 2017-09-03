package com.zarbosoft.merman.editor;

import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.syntax.style.Style;
import org.pcollections.PSet;

public abstract class Selection {
	protected abstract void clear(Context context);

	public void receiveText(final Context context, final String text) {
	}

	public abstract Visual getVisual();

	public abstract SelectionState saveState();

	public abstract Path getPath();

	public void tagsChanged(
			final Context context
	) {
		context.selectionTagsChanged();
	}

	public abstract class VisualListener {

	}

	public Style.Baked getBorderStyle(final Context context, final PSet<Tag> tags) {
		return context.getStyle(context.globalTags.plusAll(tags).plus(new PartTag("selection")));
	}

	public abstract PSet<Tag> getTags(Context context);
}
