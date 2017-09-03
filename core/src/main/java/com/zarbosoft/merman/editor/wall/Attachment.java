package com.zarbosoft.merman.editor.wall;

import com.zarbosoft.merman.editor.Context;

public abstract class Attachment {
	public void setTransverse(final Context context, final int transverse) {
	}

	public void setConverse(final Context context, final int converse) {
	}

	public void setTransverseSpan(final Context context, final int ascent, final int descent) {
	}

	public abstract void destroy(Context context);
}
