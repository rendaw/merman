package com.zarbosoft.bonestruct.editor.details;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;

public abstract class DetailsPage {
	public int priority = 0;
	public DisplayNode node;

	public abstract void tagsChanged(Context context);
}
