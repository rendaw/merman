package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.model.ObboxStyle;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;

public class VisualBorderAttachment {
	VisualAttachmentAdapter firstAdapter = new VisualAttachmentAdapter();
	VisualAttachmentAdapter lastAdapter = new VisualAttachmentAdapter();
	private final BorderAttachment border;

	public VisualBorderAttachment(final Context context, final ObboxStyle style) {
		this.border = new BorderAttachment(context, style);
		firstAdapter.addSelectionBoundsListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				border.setFirst(context, brick);
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
			}
		});
		lastAdapter.addSelectionBoundsListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
				border.setLast(context, brick);
			}
		});
	}

	public void setFirst(final Context context, final VisualNode newFirst) {
		firstAdapter.setBase(context, newFirst);
	}

	public void setLast(final Context context, final VisualNode newLast) {
		lastAdapter.setBase(context, newLast);
	}

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualNodeParent.
	 *
	 * @param context
	 * @param next
	 */
	public void notifyPreviousBrickPastEdge(final Context context, final Brick previous) {
		firstAdapter.notifyPreviousBrickPastEdge(context, previous);
	}

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualNodeParent.
	 *
	 * @param context
	 * @param next
	 */
	public void notifyNextBrickPastEdge(final Context context, final Brick next) {
		lastAdapter.notifyNextBrickPastEdge(context, next);
	}

	/**
	 * If the selection was created before a subtree has any bricks, call this when the first brick is created.
	 * This will most likely be called from createFirstBrick or createLastBrick.
	 *
	 * @param context
	 * @param out
	 */
	public void notifySeedBrick(final Context context, final Brick brick) {
		firstAdapter.notifySeedBrick(context, brick);
		lastAdapter.notifySeedBrick(context, brick);
	}

	public void setFirst(final Context context, final Brick firstBrick) {
		firstAdapter.setFirst(context, firstBrick);
	}

	public void setLast(final Context context, final Brick lastBrick) {
		lastAdapter.setLast(context, lastBrick);
	}

	public void destroy(final Context context) {
		firstAdapter.destroy(context);
		lastAdapter.destroy(context);
		border.destroy(context);
	}
}
