package com.zarbosoft.bonestruct.editor.details;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.wall.Attachment;
import com.zarbosoft.bonestruct.editor.wall.Bedding;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Wall;
import com.zarbosoft.rendaw.common.ChainComparator;

import java.util.PriorityQueue;

public class Details {
	private final PriorityQueue<DetailsPage> queue =
			new PriorityQueue<>(11, new ChainComparator<DetailsPage>().greaterFirst(m -> m.priority).build());
	public DetailsPage current;
	private Brick brick;
	private int transverse;
	private int transverseSpan;
	private int documentScroll;
	private Bedding bedding;
	private IdlePlace idle;
	private final Attachment attachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			Details.this.transverse = transverse;
			idlePlace(context);
		}

		@Override
		public void destroy(final Context context) {
			brick = null;
		}

		@Override
		public void setTransverseSpan(final Context context, final int ascent, final int descent) {
			Details.this.transverseSpan = ascent + descent;
			idlePlace(context);
		}
	};

	private void idlePlace(final Context context) {
		if (current == null)
			return;
		if (idle == null) {
			idle = new IdlePlace(context);
			context.addIdle(idle);
		}
	}

	public void setScroll(final Context context, final int scroll) {
		this.documentScroll = scroll;
		idlePlace(context);
	}

	public void tagsChanged(final Context context) {
		if (current != null)
			current.tagsChanged(context);
	}

	private class IdlePlace extends IdleTask {
		private final Context context;

		private IdlePlace(final Context context) {
			this.context = context;
		}

		@Override
		protected boolean runImplementation() {
			if (current != null) {
				setPosition(context);
			}
			return false;
		}

		@Override
		protected void destroyed() {
			idle = null;
		}
	}

	private void setPosition(final Context context) {
		current.node.setPosition(context, new Vector(
				context.syntax.detailPad.converseStart,
				transverse + transverseSpan + context.syntax.detailPad.transverseStart - documentScroll
		), false);
	}

	public Details(final Context context) {
		context.foreground.addCornerstoneListener(context, new Wall.CornerstoneListener() {
			@Override
			public void cornerstoneChanged(final Context context, final Brick cornerstone) {
				if (brick != null) {
					brick.removeAttachment(context, attachment);
				}
				brick = cornerstone;
				brick.addAttachment(context, attachment);
			}
		});
	}

	public void addPage(final Context context, final DetailsPage page) {
		queue.add(page);
		update(context);
	}

	private void update(final Context context) {
		if (queue.isEmpty()) {
			if (current != null) {
				context.foreground.removeBedding(context, bedding);
				context.midground.remove(current.node);
				bedding = null;
				current = null;
			}
		} else if (queue.peek() != current) {
			if (current != null) {
				context.midground.remove(current.node);
				context.foreground.removeBedding(context, bedding);
			}
			current = queue.peek();
			context.midground.add(current.node);
			bedding = new Bedding(
					0,
					context.syntax.detailPad.transverseStart +
							current.node.transverseSpan(context) +
							context.syntax.detailPad.transverseEnd
			);
			context.foreground.addBedding(context, bedding);
			setPosition(context);
		}
	}

	public void removePage(final Context context, final DetailsPage page) {
		if (queue.isEmpty())
			return;
		queue.remove(page);
		update(context);
	}
}
