package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;

public abstract class VisualNodeParent {
	public abstract void adjust(Context context, VisualNode.Adjustment adjustment);

	public abstract VisualNodeParent parent();

	public abstract VisualNodePart target();

	public abstract void align(Context context);

	public abstract Context.Hoverable hoverUp(Context context);

	public abstract void selectUp(Context context);
}
