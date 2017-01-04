package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;

public abstract class VisualNodeParent {
	public abstract void selectUp(Context context);

	public abstract Brick createNextBrick(Context context);

	public abstract VisualNode getNode();

	public abstract Alignment getAlignment(String alignment);

	public abstract Brick getPreviousBrick(Context context);

	public abstract Brick getNextBrick(Context context);

	public abstract Context.Hoverable hover(Context context);
}
