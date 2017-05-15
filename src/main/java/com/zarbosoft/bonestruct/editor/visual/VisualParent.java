package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtomType;
import com.zarbosoft.bonestruct.editor.wall.Brick;

public abstract class VisualParent {
	public abstract VisualParent parent();

	public abstract Brick createNextBrick(Context context);

	public abstract VisualAtomType atomVisual();

	public abstract Alignment getAlignment(String alignment);

	public abstract Brick getPreviousBrick(Context context);

	public abstract Brick getNextBrick(Context context);

	public boolean isPreviousWindowEdge(final Context context) {
		return parent() == null;
	}

	public boolean isNextWindowEdge(final Context context) {
		return parent() == null;
	}

	public abstract Hoverable hover(Context context, Vector point);

	public abstract Brick createPreviousBrick(Context context);

	public abstract Visual visual();
}
