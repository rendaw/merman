package com.zarbosoft.merman.editor.details;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;

public abstract class DetailsPage {
	public int priority = 0;
	public DisplayNode node;

	public abstract void tagsChanged(Context context);
}
