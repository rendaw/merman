package com.zarbosoft.bonestruct.editor.visual.wall;

import com.zarbosoft.bonestruct.editor.visual.Context;

public abstract class Attachment {
	public void setTransverse(final Context context, final int transverse) {
	}

	public void setConverse(final Context context, final int converse) {
	}

	public void setTransverseSpan(final Context context, final int ascent, final int descent) {
	}

	public void addAfter(final Context context, final Brick brick) {
	}

	public void addBefore(final Context context, final Brick brick) {
	}

	public abstract void destroy(Context context);
}
