package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;

public class VisualBorderAttachment extends MultiVisualAttachmentAdapter {
	private final BorderAttachment border;

	public VisualBorderAttachment(final Context context, final ObboxStyle style) {
		super(context);
		this.border = new BorderAttachment(context, style);
		addListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				border.setFirst(context, brick);
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
				border.setLast(context, brick);
			}
		});
	}

	@Override
	public void destroy(final Context context) {
		super.destroy(context);
		border.destroy(context);
	}
}
