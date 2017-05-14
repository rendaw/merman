package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.editor.Context;

public abstract class DocumentNodeParent {
	public abstract DocumentNode node();

	public abstract void selectUp(Context context);
}
