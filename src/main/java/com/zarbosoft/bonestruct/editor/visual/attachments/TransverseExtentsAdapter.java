package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.wall.Attachment;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;

import java.util.HashSet;
import java.util.Set;

public class TransverseExtentsAdapter {
	private int transverseStart;
	private int beddingBefore;
	private int endTransverseStart;
	private int endTransverseSpan;
	private int beddingAfter;
	private final Set<Listener> listeners = new HashSet<>();
	private Brick first;
	private Brick last;

	Attachment firstAttachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			transverseStart = transverse;
			notifyStartChanged(context);
		}

		@Override
		public void destroy(final Context context) {
			first = null;
		}
	};
	Brick.BeddingListener firstBeddingListener = new Brick.BeddingListener() {
		@Override
		public void beddingChanged(final Context context, final int beddingBefore, final int beddingAfter) {
			if (beddingBefore == TransverseExtentsAdapter.this.beddingBefore)
				return;
			TransverseExtentsAdapter.this.beddingBefore = beddingBefore;
			ImmutableSet.copyOf(listeners).stream().forEach(l -> l.beddingBeforeChanged(context, beddingBefore));
		}
	};

	private void notifyStartChanged(final Context context) {
		ImmutableSet.copyOf(listeners).stream().forEach(l -> l.transverseChanged(context, transverseStart));
	}

	Attachment lastAttachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			endTransverseStart = transverse;
			notifyEdgeChanged(context);
		}

		@Override
		public void setTransverseSpan(final Context context, final int ascent, final int descent) {
			endTransverseSpan = ascent + descent;
			notifyEdgeChanged(context);
		}

		@Override
		public void destroy(final Context context) {
			last = null;
		}
	};
	Brick.BeddingListener lastBeddingListener = new Brick.BeddingListener() {
		@Override
		public void beddingChanged(final Context context, final int beddingBefore, final int beddingAfter) {
			if (beddingAfter == TransverseExtentsAdapter.this.beddingAfter)
				return;
			TransverseExtentsAdapter.this.beddingAfter = beddingAfter;
			ImmutableSet.copyOf(listeners).stream().forEach(l -> l.beddingAfterChanged(context, beddingAfter));
		}
	};

	private void notifyEdgeChanged(final Context context) {
		ImmutableSet
				.copyOf(listeners)
				.stream()
				.forEach(l -> l.transverseEdgeChanged(context, endTransverseStart + endTransverseSpan));
	}

	public VisualAttachmentAdapter.BoundsListener boundsListener = new VisualAttachmentAdapter.BoundsListener() {
		@Override
		public void firstChanged(final Context context, final Brick brick) {
			if (first != null) {
				first.removeAttachment(context, firstAttachment);
				first.removeBeddingListener(firstBeddingListener);
			}
			first = brick;
			first.addAttachment(context, firstAttachment);
			first.addBeddingListener(context, firstBeddingListener);
		}

		@Override
		public void lastChanged(final Context context, final Brick brick) {
			if (last != null) {
				last.removeAttachment(context, lastAttachment);
				last.removeBeddingListener(lastBeddingListener);
			}
			last = brick;
			last.addAttachment(context, lastAttachment);
			last.addBeddingListener(context, lastBeddingListener);
		}
	};

	public void addListener(final Context context, final Listener listener) {
		listeners.add(listener);
		if (first != null) {
			listener.beddingBeforeChanged(context, beddingBefore);
			listener.transverseChanged(context, transverseStart);
		}
		if (last != null) {
			listener.beddingAfterChanged(context, beddingAfter);
			listener.transverseEdgeChanged(context, endTransverseStart + endTransverseSpan);
		}
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public static abstract class Listener {
		public abstract void transverseChanged(Context context, int transverse);

		public abstract void transverseEdgeChanged(Context context, int transverse);

		public abstract void beddingAfterChanged(Context context, int beddingAfter);

		public abstract void beddingBeforeChanged(Context context, int beddingBefore);
	}

}
