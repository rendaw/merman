package com.zarbosoft.merman.editor.details;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IterationContext;
import com.zarbosoft.merman.editor.IterationTask;
import com.zarbosoft.merman.editor.display.derived.Box;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Bedding;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.Wall;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.rendaw.common.ChainComparator;
import org.pcollections.HashTreePSet;

import java.util.PriorityQueue;

public class Details {
	private final PriorityQueue<DetailsPage> queue =
			new PriorityQueue<>(11, new ChainComparator<DetailsPage>().greaterFirst(m -> m.priority).build());
	public DetailsPage current;
	public Box background;
	private Brick brick;
	private int transverse;
	private int transverseSpan;
	private int documentScroll;
	private Bedding bedding;
	private IterationPlace idle;
	private final Attachment attachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			Details.this.transverse = transverse;
			iterationPlace(context, false);
		}

		@Override
		public void destroy(final Context context) {
			brick = null;
		}

		@Override
		public void setTransverseSpan(final Context context, final int ascent, final int descent) {
			Details.this.transverseSpan = ascent + descent;
			iterationPlace(context, false);
		}
	};

	private void iterationPlace(final Context context, final boolean animate) {
		if (current == null)
			return;
		if (idle == null) {
			idle = new IterationPlace(context);
			context.addIteration(idle);
		}
		idle.animate = idle.animate && animate;
	}

	public void setScroll(final Context context, final int scroll) {
		this.documentScroll = scroll;
		iterationPlace(context, true);
	}

	public void tagsChanged(final Context context) {
		if (current == null)
			return;
		updateStyle(context);
		place(context, false);
	}

	private Style.Baked getStyle(final Context context) {
		return context.getStyle(HashTreePSet.from(context.globalTags).plus(new PartTag("details")));
	}

	private void updateStyle(final Context context) {
		current.tagsChanged(context);
		final Style.Baked style = getStyle(context);
		if (style.box != null) {
			if (background == null) {
				background = new Box(context);
				context.midground.add(background.drawing);
			}
			background.setStyle(context, style.box);
			resizeBackground(context);
		} else {
			if (background != null) {
				context.midground.remove(background.drawing);
				background = null;
			}
		}
	}

	private class IterationPlace extends IterationTask {
		private final Context context;
		private boolean animate;

		private IterationPlace(final Context context) {
			this.context = context;
			this.animate = context.syntax.animateCoursePlacement;
		}

		@Override
		protected boolean runImplementation(final IterationContext iterationContext) {
			if (current != null) {
				place(context, animate);
			}
			return false;
		}

		@Override
		protected void destroyed() {
			idle = null;
		}
	}

	private int pageTransverse(final Context context) {
		final int padStart = context.syntax.detailPad.transverseStart;
		final int padEnd = context.syntax.detailPad.transverseEnd;
		return Math.min(
				context.transverseEdge - padStart - current.node.transverseSpan(context) - padEnd,
				-documentScroll + transverse + transverseSpan + padStart
		);
	}

	private void place(final Context context, final boolean animate) {
		final int transverse = pageTransverse(context);
		current.node.setPosition(context, new Vector(context.syntax.detailPad.converseStart, transverse), animate);
		if (background != null)
			background.setPosition(context, new Vector(0, transverse), animate);
	}

	private void resizeBackground(final Context context) {
		if (background == null)
			return;
		background.setSize(context, context.edge * 2, current.node.transverseSpan(context));
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
		context.addConverseEdgeListener(new Context.ContextIntListener() {
			@Override
			public void changed(final Context context, final int oldValue, final int newValue) {
				resizeBackground(context);
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
				context.midground.remove(current.node);
				current = null;
				if (background != null) {
					context.midground.remove(background.drawing);
					background = null;
				}
			}
		} else if (queue.peek() != current) {
			if (current != null) {
				context.midground.remove(current.node);
				context.foreground.removeBedding(context, bedding);
			} else {

			}
			current = queue.peek();
			updateStyle(context);
			place(context, false);
			context.midground.add(current.node);
			bedding = new Bedding(
					0,
					context.syntax.detailPad.transverseStart +
							current.node.transverseSpan(context) +
							context.syntax.detailPad.transverseEnd
			);
			context.foreground.addBedding(context, bedding);
		}
	}

	public void removePage(final Context context, final DetailsPage page) {
		if (queue.isEmpty())
			return;
		queue.remove(page);
		update(context);
	}
}
