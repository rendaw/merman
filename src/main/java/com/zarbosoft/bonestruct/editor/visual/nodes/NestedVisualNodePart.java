package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;

import java.util.Set;

public class NestedVisualNodePart extends EmbeddedNestedVisualNodePart {
	boolean selected = false;
	private VisualAttachmentAdapter adapter;
	private BorderAttachment border;
	Context.Hoverable hoverable;
	private NestedSelection selection;

	public NestedVisualNodePart(
			final Context context, final DataNode.Value data, final Set<Tag> tags
	) {
		super(context, data, tags);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		final Brick out = super.createFirstBrick(context);
		if (adapter != null) {
			adapter.setFirst(context, out);
			adapter.notifySeedBrick(context, out);
		}
		return out;
	}

	@Override
	public Brick createLastBrick(final Context context) {
		final Brick out = super.createLastBrick(context);
		if (adapter != null) {
			adapter.setLast(context, out);
			adapter.notifySeedBrick(context, out);
		}
		return out;
	}

	private void createAdapter(final Context context) {
		adapter = new VisualAttachmentAdapter();
		adapter.setBase(context, body);
		adapter.addListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				border.setFirst(context, brick);
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
				border.setLast(context, brick);
			}
		});
	}

	@Override
	public boolean select(final Context context) {
		if (selected)
			throw new AssertionError("Already selected");
		else if (border != null) {
			context.clearHover();
		}
		selected = true;
		border = new BorderAttachment(context, context.syntax.selectStyle);
		createAdapter(context);
		context.setSelection(new NestedSelection());
		return true;
	}

	protected Iterable<Context.Action> getActions(final Context context) {
		return ImmutableList.of(new Context.Action() {
			@Override
			public void run(final Context context) {
				body.select(context);
			}

			@Override
			public String getName() {
				return "enter";
			}
		}, new Context.Action() {
			@Override
			public void run(final Context context) {
				if (parent != null) {
					parent.selectUp(context);
				}
			}

			@Override
			public String getName() {
				return "exit";
			}
		}, new Context.Action() {
			@Override
			public void run(final Context context) {
				context.history.apply(context, new DataNode.ChangeSet(data, context.syntax.gap.create()));
			}

			@Override
			public String getName() {
				return "delete";
			}
		}, new Context.Action() {
			@Override
			public void run(final Context context) {

			}

			@Override
			public String getName() {
				return "copy";
			}
		}, new Context.Action() {
			@Override
			public void run(final Context context) {

			}

			@Override
			public String getName() {
				return "cut";
			}
		}, new Context.Action() {
			@Override
			public void run(final Context context) {

			}

			@Override
			public String getName() {
				return "paste";
			}
		});
	}

	private class NestedSelection extends Context.Selection {
		@Override
		public void clear(final Context context) {
			border.destroy(context);
			border = null;
			adapter.destroy(context);
			adapter = null;
			selected = false;
		}

		@Override
		protected Hotkeys getHotkeys(final Context context) {
			return context.getHotkeys(tags());
		}

		@Override
		public Iterable<Context.Action> getActions(final Context context) {
			return NestedVisualNodePart.this.getActions(context);
		}

		@Override
		public VisualNodePart getVisual() {
			return NestedVisualNodePart.this;
		}

		@Override
		public void addBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			adapter.addListener(context, listener);

		}

		@Override
		public void removeBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			adapter.removeListener(context, listener);
		}
	}

	@Override
	void set(final Context context, final Node data) {
		super.set(context, data);
		if (adapter != null) {
			adapter.setBase(context, body);
		}
	}

	@Override
	protected VisualNodeParent createParent() {
		return new NestedParent();
	}

	private class NestedParent extends EmbeddedNestedVisualNodePart.NestedParent {
		@Override
		public Brick createNextBrick(final Context context) {
			Brick out = null;
			if (parent != null)
				out = parent.createNextBrick(context);
			if (adapter != null)
				adapter.notifyNextBrickPastEdge(context, out);
			return out;
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			final Brick out = super.createPreviousBrick(context);
			if (adapter != null)
				adapter.notifyPreviousBrickPastEdge(context, out);
			return out;
		}

		@Override
		public Context.Hoverable hover(final Context context, final Vector point) {
			if (selected)
				return null;
			{
				final Context.Hoverable parentHoverable = super.hover(context, point);
				if (parentHoverable != null)
					return parentHoverable;
			}
			if (hoverable != null)
				return hoverable;
			border = new BorderAttachment(context, context.syntax.hoverStyle);
			createAdapter(context);
			hoverable = new Context.Hoverable() {
				@Override
				public void clear(final Context context) {
					border.destroy(context);
					border = null;
					adapter.destroy(context);
					adapter = null;
					hoverable = null;
				}

				@Override
				public void click(final Context context) {
					select(context);
				}

				@Override
				public NodeType.NodeTypeVisual node() {
					if (NestedVisualNodePart.this.parent == null)
						return null;
					return NestedVisualNodePart.this.parent.getNode();
				}

				@Override
				public VisualNodePart part() {
					return NestedVisualNodePart.this;
				}
			};
			return hoverable;
		}
	}
}
