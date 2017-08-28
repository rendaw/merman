package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.wall.Attachment;
import com.zarbosoft.bonestruct.editor.wall.Brick;

import java.util.HashSet;
import java.util.Set;

public class VisualAttachmentAdapter {
	private Visual base;
	private Brick first;
	private Brick last;
	private final Set<BoundsListener> boundsListeners = new HashSet<>();
	private final Attachment firstAttachment = new Attachment() {
		@Override
		public void destroy(final Context context) {
			setFirst(context, base.getFirstBrick(context));
		}
	};
	private final Attachment lastAttachment = new Attachment() {
		@Override
		public void destroy(final Context context) {
			setLast(context, base.getLastBrick(context));
		}
	};

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualParent.
	 *
	 * @param next
	 * @param context
	 */
	public void notifyPreviousBrickPastEdge(final Context context) {
		setFirst(context, base.getFirstBrick(context));
	}

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualParent.
	 *
	 * @param context
	 */
	public void notifyNextBrickPastEdge(final Context context) {
		setLast(context, base.getLastBrick(context));
	}

	/**
	 * If the selection was created before a subtree has any bricks, call this when the first brick is created.
	 * This will most likely be called from createFirstBrick or createLastBrick.
	 *
	 * @param context
	 * @param out
	 */
	public void notifySeedBrick(final Context context, final Brick out) {
		if (out == null)
			return;
		if (first == null)
			setFirst(context, out);
		if (last == null)
			setLast(context, out);
	}

	public void destroy(final Context context) {
		if (first != null)
			first.removeAttachment(context, firstAttachment);
		if (last != null)
			last.removeAttachment(context, lastAttachment);
	}

	public abstract static class BoundsListener {
		public abstract void firstChanged(Context context, Brick brick);

		public abstract void lastChanged(Context context, Brick brick);
	}

	public void addListener(final Context context, final BoundsListener listener) {
		boundsListeners.add(listener);
		if (first != null)
			listener.firstChanged(context, first);
		if (last != null)
			listener.lastChanged(context, last);
	}

	public void removeListener(final Context context, final BoundsListener listener) {
		boundsListeners.remove(listener);
	}

	public void setBase(final Context context, final Visual base) {
		this.base = base;
		setFirst(context, base.getFirstBrick(context));
		setLast(context, base.getLastBrick(context));
	}

	public void setFirst(final Context context, final Brick firstBrick) {
		if (first == firstBrick)
			return;
		if (first != null)
			first.removeAttachment(context, firstAttachment);
		first = firstBrick;
		boundsListeners.forEach(l -> l.firstChanged(context, firstBrick));
		if (first == null)
			return;
		first.addAttachment(context, firstAttachment);
	}

	public void setLast(final Context context, final Brick lastBrick) {
		if (last == lastBrick)
			return;
		if (last != null)
			last.removeAttachment(context, lastAttachment);
		last = lastBrick;
		boundsListeners.forEach(l -> l.lastChanged(context, lastBrick));
		if (last == null)
			return;
		last.addAttachment(context, lastAttachment);
	}
}
