package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public interface Blank extends DisplayNode {
	void setConverseSpan(Context context, int converse);

	void setTransverseSpan(Context context, int transverse);
}
