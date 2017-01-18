package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.pidgoon.internal.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;
import java.util.Set;

public class NestedVisualNodePart extends VisualNodePart {
	private final DataNode.Value data;
	protected VisualNode body;
	VisualNodeParent parent;
	boolean selected = false;
	private VisualBorderAttachment border;
	Context.Hoverable hoverable;
	private Context.Selection selection;

	public NestedVisualNodePart(final Context context, final DataNode.Value data, final Set<Tag> tags) {
		super(tags);
		this.data = data;
		data.addListener(new DataNode.Listener() {

			@Override
			public void set(final Context context, final Node node) {
				NestedVisualNodePart.this.set(context, node);
			}

		});
		set(context, data.get());
	}

	protected VisualNodeParent createParent() {
		return new VisualNodeParent() {
			@Override
			public void selectUp(final Context context) {
				select(context);
			}

			@Override
			public Brick createNextBrick(final Context context) {
				Brick out = null;
				if (parent != null)
					out = parent.createNextBrick(context);
				if (border != null)
					border.notifyNextBrickPastEdge(context, out);
				return out;
			}

			@Override
			public Brick createPreviousBrick(final Context context) {
				Brick out = null;
				if (parent != null)
					out = parent.createPreviousBrick(context);
				if (border != null)
					border.notifyPreviousBrickPastEdge(context, out);
				return out;
			}

			@Override
			public VisualNode getTarget() {
				return NestedVisualNodePart.this;
			}

			@Override
			public VisualNode getNode() {
				throw new NotImplementedException();
			}

			@Override
			public Alignment getAlignment(final String alignment) {
				return parent.getAlignment(alignment);
			}

			@Override
			public Brick getPreviousBrick(final Context context) {
				if (parent == null)
					return null;
				return parent.getPreviousBrick(context);
			}

			@Override
			public Brick getNextBrick(final Context context) {
				if (parent == null)
					return null;
				return parent.getNextBrick(context);
			}

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				if (selected)
					return null;
				if (parent != null) {
					final Context.Hoverable parentHoverable = parent.hover(context, point);
					if (parentHoverable != null)
						return parentHoverable;
				}
				if (hoverable != null)
					return hoverable;
				border = new VisualBorderAttachment(context, context.syntax.hoverStyle);
				border.setFirst(context, body);
				border.setLast(context, body);
				hoverable = new Context.Hoverable() {
					@Override
					public void clear(final Context context) {
						border.destroy(context);
						border = null;
						hoverable = null;
					}

					@Override
					public void click(final Context context) {
						select(context);
					}
				};
				return hoverable;
			}
		};
	}

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		final Brick out = body.createFirstBrick(context);
		if (border != null) {
			border.setFirst(context, out);
			border.notifySeedBrick(context, out);
		}
		return out;
	}

	@Override
	public Brick createLastBrick(final Context context) {
		final Brick out = body.createLastBrick(context);
		if (border != null) {
			border.setLast(context, out);
			border.notifySeedBrick(context, out);
		}
		return out;
	}

	@Override
	public boolean select(final Context context) {
		if (selected)
			throw new AssertionError("Already selected");
		else if (border != null) {
			context.clearHover();
		}
		selected = true;
		border = new VisualBorderAttachment(context, context.syntax.selectStyle);
		border.setFirst(context, body);
		border.setLast(context, body);
		context.setSelection(new Context.Selection() {
			@Override
			public void clear(final Context context) {
				border.destroy(context);
				border = null;
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
		});
		return true;
	}

	private void set(final Context context, final Node data) {
		if (body != null)
			body.destroyBricks(context);
		this.body = data.createVisual(context);
		if (border != null) {
			border.setFirst(context, body);
			border.setLast(context, body);
		}
		body.setParent(createParent());
		if (parent != null) {
			final Brick previousBrick = parent.getPreviousBrick(context);
			final Brick nextBrick = parent.getNextBrick(context);
			if (previousBrick != null && nextBrick != null)
				context.fillFromEndBrick(previousBrick);
		}
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
				context.history.apply(context, new DataNode.ChangeSet(data, context.syntax.bud.create()));
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

	@Override
	public Brick getFirstBrick(final Context context) {
		return body.getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		return body.getLastBrick(context);
	}

	@Override
	public void rootAlignments(final Context context, final Map<String, Alignment> alignments) {
		body.rootAlignments(context, alignments);
	}

	@Override
	public void destroyBricks(final Context context) {
		body.destroyBricks(context);
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return body.getPropertiesForTagsChange(context, change);
	}
}
