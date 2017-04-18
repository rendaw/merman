package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.syntax.NodeType;

public abstract class Hoverable {
	protected abstract void clear(Context context);

	public abstract void click(Context context);

	public abstract NodeType.NodeTypeVisual node();

	public abstract VisualPart part();
}
