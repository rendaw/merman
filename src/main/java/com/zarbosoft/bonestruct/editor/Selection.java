package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;

public abstract class Selection {
	protected abstract void clear(Context context);

	public void receiveText(final Context context, final String text) {
	}

	public abstract VisualPart getVisual();

	public abstract SelectionState saveState();

	public abstract Path getPath();

	public abstract class VisualListener {

	}

	public abstract void addBrickListener(Context context, final VisualAttachmentAdapter.BoundsListener listener);

	public abstract void removeBrickListener(
			Context context, final VisualAttachmentAdapter.BoundsListener listener
	);
}
