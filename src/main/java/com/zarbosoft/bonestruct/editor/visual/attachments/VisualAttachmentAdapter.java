package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.wall.Attachment;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;

import java.util.HashSet;
import java.util.Set;

public class VisualAttachmentAdapter {
	private VisualNode base;
	Brick first;
	private final Set<BoundsListener> boundsListeners = new HashSet<>();
	private final Attachment firstAttachment = new Attachment() {
		@Override
		public void addBefore(final Context context, final Brick brick) {
			if (brick == ignoreFirst)
				return;
			boundsListeners.forEach(l -> l.firstChanged(context, brick));
			setFirst(context, brick);
		}

		@Override
		public void destroy(final Context context) {
			first = null;
			setFirst(context, base.getFirstBrick(context));
		}
	};
	private final Attachment lastAttachment = new Attachment() {
		@Override
		public void addAfter(final Context context, final Brick brick) {
			if (brick == ignoreLast)
				return;
			boundsListeners.forEach(l -> l.lastChanged(context, brick));
			setLast(context, brick);
		}

		@Override
		public void destroy(final Context context) {
			last = null;
			setLast(context, base.getLastBrick(context));
		}
	};
	private Brick ignoreFirst;
	private Brick ignoreLast;
	private Brick last;

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualNodeParent.
	 *
	 * @param context
	 * @param next
	 */
	public void notifyPreviousBrickPastEdge(final Context context, final Brick previous) {
		ignoreFirst = previous;
	}

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualNodeParent.
	 *
	 * @param context
	 * @param next
	 */
	public void notifyNextBrickPastEdge(final Context context, final Brick next) {
		ignoreLast = next;
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

	public void setBase(final Context context, final VisualNode base) {
		this.base = base;
		setFirst(context, base.getFirstBrick(context));
		setLast(context, base.getLastBrick(context));
	}

	public void setFirst(final Context context, final Brick firstBrick) {
		if (first != null)
			first.removeAttachment(context, firstAttachment);
		first = firstBrick;
		if (first == null)
			return;
		boundsListeners.forEach(l -> l.firstChanged(context, firstBrick));
		first.addAttachment(context, firstAttachment);
	}

	public void setLast(final Context context, final Brick lastBrick) {
		if (last != null)
			last.removeAttachment(context, lastAttachment);
		last = lastBrick;
		if (last == null)
			return;
		boundsListeners.forEach(l -> l.lastChanged(context, lastBrick));
		last.addAttachment(context, lastAttachment);
	}
}
