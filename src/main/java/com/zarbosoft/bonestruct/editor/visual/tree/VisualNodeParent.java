package com.zarbosoft.bonestruct.editor.visual.tree;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.wall.Brick;

public abstract class VisualNodeParent {
	public abstract void selectUp(Context context);

	public abstract Brick createNextBrick(Context context);

	public abstract NodeType.NodeTypeVisual getNode();

	public abstract Alignment getAlignment(String alignment);

	public abstract Brick getPreviousBrick(Context context);

	public abstract Brick getNextBrick(Context context);

	public abstract Hoverable hover(Context context, Vector point);

	public abstract Brick createPreviousBrick(Context context);

	public abstract VisualNode getTarget();
}
