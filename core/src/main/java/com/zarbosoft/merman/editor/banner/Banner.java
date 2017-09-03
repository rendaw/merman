package com.zarbosoft.merman.editor.banner;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.IdleTask;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.merman.editor.display.Text;
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
import java.util.Timer;
import java.util.TimerTask;

public class Banner {
	public Text text;
	public Box background;
	private final PriorityQueue<BannerMessage> queue =
			new PriorityQueue<>(11, new ChainComparator<BannerMessage>().greaterFirst(m -> m.priority).build());
	private BannerMessage current;
	private final Timer timer = new Timer();
	private Brick brick;
	private int transverse;
	private int scroll;
	private Bedding bedding;
	private IdlePlace idle;
	private final Attachment attachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			Banner.this.transverse = transverse;
			idlePlace(context, false);
		}

		@Override
		public void destroy(final Context context) {
			brick = null;
		}
	};

	private void idlePlace(final Context context, final boolean animate) {
		if (text == null)
			return;
		if (idle == null) {
			idle = new IdlePlace(context);
			context.addIdle(idle);
		}
		idle.animate = idle.animate && animate;
	}

	public void setScroll(final Context context, final int scroll) {
		this.scroll = scroll;
		idlePlace(context, true);
	}

	public void tagsChanged(final Context context) {
		updateStyle(context);
	}

	private class IdlePlace extends IdleTask {
		private final Context context;
		public boolean animate;

		private IdlePlace(final Context context) {
			this.context = context;
			animate = context.syntax.animateCoursePlacement;
		}

		@Override
		protected boolean runImplementation() {
			place(context, animate);
			return false;
		}

		@Override
		protected void destroyed() {
			idle = null;
		}
	}

	private void place(final Context context, final boolean animate) {
		if (text == null)
			return;
		final int calculatedTransverse =
				transverse - text.font().getDescent() - context.syntax.bannerPad.transverseEnd - scroll;
		text.setPosition(context, new Vector(context.syntax.bannerPad.converseStart, calculatedTransverse), animate);
		if (background != null)
			background.setPosition(context, new Vector(0, calculatedTransverse - text.font().getAscent()), animate);
	}

	private void resizeBackground(final Context context) {
		if (background == null)
			return;
		final Font font = text.font();
		background.setSize(context, context.edge * 2, font.getDescent() + font.getAscent());
	}

	public Banner(final Context context) {
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

	public void addMessage(final Context context, final BannerMessage message) {
		if (queue.isEmpty()) {
			final Style.Baked style = getStyle(context);
			if (style.box != null) {
				background = new Box(context);
				context.midground.add(background.drawing);
			}
			text = context.display.text();
			context.midground.add(text);
			updateStyle(context);
			resizeBackground(context);
		}
		queue.add(message);
		update(context);
	}

	private Style.Baked getStyle(final Context context) {
		return context.getStyle(HashTreePSet.from(context.globalTags).plus(new PartTag("banner")));
	}

	private void updateStyle(final Context context) {
		if (text == null)
			return;
		final Style.Baked style = getStyle(context);
		background.setStyle(context, style.box);
		text.setFont(context, style.getFont(context));
		text.setColor(context, style.color);
		if (bedding != null)
			context.foreground.removeBedding(context, bedding);
		bedding = new Bedding(text.transverseSpan(context) +
				context.syntax.bannerPad.transverseStart +
				context.syntax.bannerPad.transverseEnd, 0);
		context.foreground.addBedding(context, bedding);
		idlePlace(context, true);
	}

	private void update(final Context context) {
		if (queue.isEmpty()) {
			if (text != null) {
				context.midground.remove(text);
				text = null;
				if (background != null) {
					context.midground.remove(background.drawing);
					background = null;
				}
				context.foreground.removeBedding(context, bedding);
				bedding = null;
			}
		} else if (queue.peek() != current) {
			current = queue.peek();
			text.setText(context, current.text);
			timer.purge();
			if (current.duration != null)
				try {
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							context.addIdle(new IdleTask() {
								@Override
								protected boolean runImplementation() {
									queue.poll();
									update(context);
									return false;
								}

								@Override
								protected void destroyed() {

								}
							});
						}
					}, current.duration.toMillis());
				} catch (final IllegalStateException e) {
					// While shutting down
				}
		}
	}

	public void destroy(final Context context) {
		timer.cancel();
	}

	public void removeMessage(final Context context, final BannerMessage message) {
		if (queue.isEmpty())
			return; // TODO implement message destroy cb, extraneous removeMessages unnecessary
		queue.remove(message);
		if (queue.isEmpty())
			timer.purge();
		update(context);
	}
}
