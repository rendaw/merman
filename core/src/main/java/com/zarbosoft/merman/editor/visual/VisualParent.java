package com.zarbosoft.merman.editor.visual;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;

import java.util.ArrayList;

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

	public void bricksCreated(final Context context, final ArrayList<Brick> bricks) {
		context.bricksCreated(visual(), bricks);
	}

	public void firstBrickChanged(final Context context, final Brick firstBrick) {
	}

	public void lastBrickChanged(final Context context, final Brick lastBrick) {
	}
}
