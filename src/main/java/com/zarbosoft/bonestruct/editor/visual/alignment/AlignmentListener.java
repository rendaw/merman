package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.visual.Context;

public interface AlignmentListener {
	void align(Context context);

	int getMinConverse(Context context);
}
