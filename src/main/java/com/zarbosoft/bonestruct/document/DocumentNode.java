package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.editor.visual.Visual;

public abstract class DocumentNode {
	public abstract Visual visual();

	public abstract DocumentNodeParent parent();
}
