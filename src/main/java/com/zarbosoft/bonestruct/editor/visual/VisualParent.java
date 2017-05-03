package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeType;
import com.zarbosoft.bonestruct.wall.Brick;

public abstract class VisualParent {
	public abstract void selectUp(Context context);

	public abstract Brick createNextBrick(Context context);

	public abstract VisualNodeType getNodeVisual();

	public abstract Alignment getAlignment(String alignment);

	public abstract Brick getPreviousBrick(Context context);

	public abstract Brick getNextBrick(Context context);

	public abstract Hoverable hover(Context context, Vector point);

	public abstract Brick createPreviousBrick(Context context);

	public abstract Visual getTarget();
}
