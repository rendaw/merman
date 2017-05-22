package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.syntax.style.Style;
import org.pcollections.PSet;

public abstract class Selection {
	protected abstract void clear(Context context);

	public void receiveText(final Context context, final String text) {
	}

	public abstract VisualPart getVisual();

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
