package com.zarbosoft.bonestruct.editor.details;

import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.wall.Attachment;
import com.zarbosoft.bonestruct.wall.Bedding;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.rendaw.common.ChainComparator;
import javafx.scene.Group;
import javafx.scene.shape.Rectangle;

import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;

public class Details {
	private final static int maxTransverse = 300;
	private Group group;
	private final PriorityQueue<DetailsPage> queue =
			new PriorityQueue<>(11, new ChainComparator<DetailsPage>().greaterFirst(m -> m.priority).build());
	private DetailsPage current;
	private Brick brick;
	private int transverse;
	private int transverseSpan;
	private int documentScroll;
	private int detailsScroll;
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

	private class IdlePlace extends IdleTask {
		private final Context context;

		private IdlePlace(final Context context) {
			this.context = context;
		}

		@Override
		protected void runImplementation() {
			if (current != null) {
				translateGroup(context);
			}
			idle = null;
		}

		@Override
		protected void destroyed() {
			idle = null;
		}
	}

	public Details(final Context context) {
		context.addSelectionListener(new Context.SelectionListener() {
			@Override
			public void selectionChanged(final Context context, final Selection selection) {
				selection.addBrickListener(context, new VisualAttachmentAdapter.BoundsListener() {
					@Override
					public void firstChanged(final Context context, final Brick first) {
						if (brick != null) {
							if (bedding != null)
								brick.removeBedding(context, bedding);
							brick.removeAttachment(context, attachment);
						}
						brick = first;
						if (bedding != null) {
							brick.addBedding(context, bedding);
						}
						brick.addAttachment(context, attachment);
					}

					@Override
					public void lastChanged(final Context context, final Brick last) {

					}
				});
			}
		});
		context.addActionSource(new ActionSource() {
			@Override
			public Stream<Action> getActions(
					final Context context, final Set<VisualNode.Tag> tags
			) {
				return Stream.of(new Action() {
					@Override
					public void run(final Context context) {
						scroll(context, detailsScroll - 100);
					}

					@Override
					public String getName() {
						return "details-back";
					}
				}, new Action() {
					@Override
					public void run(final Context context) {
						scroll(context, detailsScroll + 100);
					}

					@Override
					public String getName() {
						return "details-forward";
					}
				}, new Action() {
					@Override
					public void run(final Context context) {
						scroll(context, 0);
					}

					@Override
					public String getName() {
						return "details-start";
					}
				}, new Action() {
					@Override
					public void run(final Context context) {
						if (current == null)
							return;
						scroll(context, context.sceneGetTransverseSpan(current.node) - maxTransverse);
					}

					@Override
					public String getName() {
						return "details-end";
					}
				});
			}
		});
	}

	private void scroll(final Context context, int newScroll) {
		if (current == null)
			return;
		newScroll = Math.min(newScroll, context.sceneGetTransverseSpan(current.node) - maxTransverse);
		newScroll = Math.max(newScroll, 0);
		detailsScroll = newScroll;
		translateGroup(context);
		group.setClip(new Rectangle(0, detailsScroll, 100000, maxTransverse));
	}

	private void translateGroup(final Context context) {
		context.translate(current.node, new Vector(0, Math.min(
				context.transverseEdge - Math.min(maxTransverse, context.sceneGetTransverseSpan(current.node)),
				transverse + transverseSpan - detailsScroll
		)));
	}

	public void addPage(final Context context, final DetailsPage page) {
		if (queue.isEmpty()) {
			group = new Group();
			context.display.background.getChildren().add(group);
		}
		queue.add(page);
		update(context);
	}

	private void update(final Context context) {
		if (queue.isEmpty()) {
			if (group != null) {
				context.display.background.getChildren().remove(group);
				group = null;
				brick.removeBedding(context, bedding);
				bedding = null;
			}
		} else if (queue.peek() != current) {
			current = queue.peek();
			group.getChildren().clear();
			group.getChildren().add(current.node);
			if (bedding != null) {
				brick.removeBedding(context, bedding);
			}
			bedding = new Bedding(0, Math.min(maxTransverse, context.sceneGetTransverseSpan(current.node)));
			brick.addBedding(context, bedding);
			scroll(context, 0);
		}
	}

	public void removePage(final Context context, final DetailsPage page) {
		if (queue.isEmpty())
			return;
		queue.remove(page);
		update(context);
	}
}
