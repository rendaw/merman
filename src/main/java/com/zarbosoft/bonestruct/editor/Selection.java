package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;

public abstract class Selection {
	protected abstract void clear(Context context);

	public void receiveText(final Context context, final String text) {
	}

	public abstract VisualNodePart getVisual();

	public abstract class VisualListener {

	}

	public abstract void addBrickListener(Context context, final VisualAttachmentAdapter.BoundsListener listener);

	public abstract void removeBrickListener(
			Context context, final VisualAttachmentAdapter.BoundsListener listener
	);
}
