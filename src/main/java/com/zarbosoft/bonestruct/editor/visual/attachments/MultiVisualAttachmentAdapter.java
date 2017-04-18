package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.wall.Brick;

import java.util.HashSet;
import java.util.Set;

public class MultiVisualAttachmentAdapter {
	private Brick first;
	private Brick last;
	private final VisualAttachmentAdapter firstAdapter = new VisualAttachmentAdapter();
	private final VisualAttachmentAdapter lastAdapter = new VisualAttachmentAdapter();
	private final Set<VisualAttachmentAdapter.BoundsListener> listeners = new HashSet<>();

	public MultiVisualAttachmentAdapter(final Context context) {
		firstAdapter.addListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				first = brick;
				ImmutableSet.copyOf(listeners).stream().forEach(l -> l.firstChanged(context, brick));
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
			}
		});
		lastAdapter.addListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
				last = brick;
				ImmutableSet.copyOf(listeners).stream().forEach(l -> l.lastChanged(context, brick));
			}
		});
	}

	public void addListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
		listeners.add(listener);
		if (first != null)
			listener.firstChanged(context, first);
		if (last != null)
			listener.lastChanged(context, last);
	}

	public void removeListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
		listeners.remove(listener);
	}

	public void setFirst(final Context context, final Visual newFirst) {
		firstAdapter.setBase(context, newFirst);
	}

	public void setLast(final Context context, final Visual newLast) {
		lastAdapter.setBase(context, newLast);
	}

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualParent.
	 *
	 * @param context
	 * @param next
	 */
	public void notifyPreviousBrickPastEdge(final Context context, final Brick previous) {
		firstAdapter.notifyPreviousBrickPastEdge(context, previous);
	}

	/**
	 * The next brick to be added is outside the visual subtree so should be ignored.
	 * This will most likely be called from the target's VisualParent.
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
	}
}
