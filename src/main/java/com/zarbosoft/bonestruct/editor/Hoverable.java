package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeType;

public abstract class Hoverable {
	protected abstract void clear(Context context);

	public abstract void click(Context context);

	public abstract VisualNodeType node();

	public abstract VisualPart part();
}
