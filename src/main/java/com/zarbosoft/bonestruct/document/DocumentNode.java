package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.editor.visual.Visual;

public abstract class DocumentNode {
	public abstract Visual getVisual();

	public abstract DocumentNodeParent parent();
}
