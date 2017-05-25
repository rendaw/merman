package com.zarbosoft.bonestruct.editor.banner;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.wall.Attachment;
import com.zarbosoft.bonestruct.editor.wall.Bedding;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Wall;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.rendaw.common.ChainComparator;
import org.pcollections.HashTreePSet;

import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;

public class Banner {
	public Text text;
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
			idlePlace(context);
		}

		@Override
		public void destroy(final Context context) {
			brick = null;
		}
	};

	private void idlePlace(final Context context) {
		if (text == null)
			return;
		if (idle == null) {
			idle = new IdlePlace(context);
			context.addIdle(idle);
		}
	}

	public void setScroll(final Context context, final int scroll) {
		this.scroll = scroll;
		idlePlace(context);
	}

	public void tagsChanged(final Context context) {
		updateStyle(context);
	}

	private class IdlePlace extends IdleTask {
		private final Context context;

		private IdlePlace(final Context context) {
			this.context = context;
		}

		@Override
		protected boolean runImplementation() {
			setPosition(context);
			return false;
		}

		@Override
		protected void destroyed() {
			idle = null;
		}
	}

	private void setPosition(final Context context) {
		if (text == null)
			return;
		text.setPosition(context, new Vector(
				context.syntax.bannerPad.converseStart,
				transverse - text.font().getDescent() - context.syntax.bannerPad.transverseEnd - scroll
		), false);
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
	}

	public void addMessage(final Context context, final BannerMessage message) {
		if (queue.isEmpty()) {
			text = context.display.text();
			final Style.Baked style = getStyle(context);
			text.setFont(context, style.getFont(context));
			text.setColor(context, style.color);
			context.midground.add(text);
			bedding = new Bedding(text.transverseSpan(context) +
					context.syntax.bannerPad.transverseStart +
					context.syntax.bannerPad.transverseEnd, 0);
			context.foreground.addBedding(context, bedding);
			setPosition(context);
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
		text.setFont(context, style.getFont(context));
		text.setColor(context, style.color);
		context.foreground.removeBedding(context, bedding);
		bedding = new Bedding(text.transverseSpan(context) +
				context.syntax.bannerPad.transverseStart +
				context.syntax.bannerPad.transverseEnd, 0);
		context.foreground.addBedding(context, bedding);
		idlePlace(context);
	}

	private void update(final Context context) {
		if (queue.isEmpty()) {
			if (text != null) {
				context.midground.remove(text);
				text = null;
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
