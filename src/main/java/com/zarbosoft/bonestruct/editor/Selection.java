package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.syntax.style.Style;

public abstract class Selection {
	protected abstract void clear(Context context);

	public void receiveText(final Context context, final String text) {
	}

	public abstract VisualPart getVisual();

	public abstract SelectionState saveState();

	public abstract Path getPath();

	public abstract void globalTagsChanged(Context context);

	public abstract class VisualListener {

	}

	public Style.Baked getStyle(final Context context) {
		return context.getStyle(context.globalTags.plus(new Visual.PartTag("select")));
	}
}
