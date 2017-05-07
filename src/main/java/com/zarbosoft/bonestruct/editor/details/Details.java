package com.zarbosoft.bonestruct.editor.details;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.Selection;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.wall.Attachment;
import com.zarbosoft.bonestruct.editor.wall.Bedding;
import com.zarbosoft.bonestruct.editor.wall.Brick;
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
			idle = null;
			return false;
		}

		@Override
		protected void destroyed() {
			idle = null;
		}
	}

	private void setPosition(final Context context) {
		current.node.setTransverse(context, Math.min(
				context.transverseEdge - current.node.transverseSpan(context),
				transverse + transverseSpan - documentScroll
		), false);
	}

	public Details(final Context context) {
		context.addSelectionListener(new Context.SelectionListener() {
			@Override
			public void selectionChanged(final Context context, final Selection selection) {
				selection.addBrickListener(context, new VisualAttachmentAdapter.BoundsListener() {
					@Override
					public void firstChanged(final Context context, final Brick first) {
						if (brick != null) {
							brick.removeAttachment(context, attachment);
						}
						brick = first;
						brick.addAttachment(context, attachment);
					}

					@Override
					public void lastChanged(final Context context, final Brick last) {

					}
				});
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
				bedding = null;
				current = null;
			}
		} else if (queue.peek() != current) {
			if (current != null) {
				context.midground.remove(current.node);
			}
			current = queue.peek();
			context.midground.add(current.node);
			if (bedding != null) {
				context.foreground.removeBedding(context, bedding);
			}
			bedding = new Bedding(0, current.node.transverseSpan(context));
			context.foreground.addBedding(context, bedding);
			setPosition(context);
		}
	}

	public void removePage(final Context context, final DetailsPage page) {
		if (queue.isEmpty())
			return;
		if (current == page) {
			context.midground.remove(page.node);
		}
		queue.remove(page);
		update(context);
	}
}
