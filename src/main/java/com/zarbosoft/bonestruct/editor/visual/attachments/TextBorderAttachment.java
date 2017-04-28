package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.raw.Obbox;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;
import com.zarbosoft.bonestruct.wall.Attachment;
import com.zarbosoft.bonestruct.wall.bricks.BrickText;

public class TextBorderAttachment {
	BrickText first;
	int firstIndex;
	BrickText last;
	int lastIndex;
	private final Obbox border;
	private final Attachment firstAttachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			startTransverse = transverse;
			redraw(context);
		}

		@Override
		public void setConverse(final Context context, final int converse) {
			startConverse = converse;
			redraw(context);
		}

		@Override
		public void setTransverseSpan(final Context context, final int ascent, final int descent) {
			startTransverseSpan = ascent + descent;
			redraw(context);
		}

		@Override
		public void destroy(final Context context) {
			first = null;
		}
	};
	private final Attachment lastAttachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			endTransverse = transverse;
			redraw(context);
		}

		@Override
		public void setConverse(final Context context, final int converse) {
			endConverse = converse;
			redraw(context);
		}

		@Override
		public void setTransverseSpan(final Context context, final int ascent, final int descent) {
			endTransverseSpan = ascent + descent;
			redraw(context);
		}

		@Override
		public void destroy(final Context context) {
			last = null;
		}
	};
	private int startConverse;
	private int startTransverse;
	private int startTransverseSpan;
	private int endConverse;
	private int endTransverse;
	private int endTransverseSpan;

	public TextBorderAttachment(
			final Context context, final ObboxStyle.Baked style
	) {
		border = Obbox.fromSettings(context, style);
		context.background.add(border.drawing);
	}

	public void setFirst(final Context context, final BrickText first) {
		if (this.first != null)
			this.first.removeAttachment(context, this.firstAttachment);
		this.first = first;
		if (first == null)
			return;
		this.first.addAttachment(context, this.firstAttachment);
	}

	public void setFirstIndex(final Context context, final int index) {
		firstIndex = index;
		redraw(context);
	}

	public void setLast(final Context context, final BrickText last) {
		if (this.last != null)
			this.last.removeAttachment(context, this.lastAttachment);
		this.last = last;
		if (last == null)
			return;
		this.last.addAttachment(context, this.lastAttachment);
	}

	public void setLastIndex(final Context context, final int index) {
		lastIndex = index;
		redraw(context);
	}

	public void destroy(final Context context) {
		if (first != null)
			first.removeAttachment(context, this.firstAttachment);
		if (last != null)
			last.removeAttachment(context, this.lastAttachment);
		context.background.remove(border.drawing);
	}

	public void redraw(final Context context) {
		if (first == null)
			return;
		if (last == null)
			return;
		border.setSize(
				context,
				startConverse + first.getConverseOffset(firstIndex),
				startTransverse,
				startTransverse + startTransverseSpan,
				endConverse + last.getConverseOffset(lastIndex),
				endTransverse,
				endTransverse + endTransverseSpan
		);
	}
}
