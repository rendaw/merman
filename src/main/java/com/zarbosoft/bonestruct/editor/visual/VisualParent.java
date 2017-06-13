package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.editor.wall.Brick;

public abstract class VisualParent {
	public abstract Visual visual();

	public abstract VisualAtom atomVisual();

	public abstract Brick createPreviousBrick(Context context);

	public abstract Brick createNextBrick(Context context);

	public abstract Brick getPreviousBrick(Context context);

	public abstract Brick getNextBrick(Context context);

	public abstract Hoverable hover(Context context, Vector point);

	public abstract boolean selectPrevious(Context context);

	public abstract boolean selectNext(Context context);

}
