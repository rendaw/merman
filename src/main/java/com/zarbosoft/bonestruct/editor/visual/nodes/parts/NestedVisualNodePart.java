package com.zarbosoft.bonestruct.editor.visual.nodes.parts;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Hotkeys;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
import com.zarbosoft.pidgoon.internal.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class NestedVisualNodePart extends VisualNodePart {
	protected final VisualNode body;
	VisualNodeParent parent;
	boolean selected = false;
	private BorderAttachment border;
	Context.Hoverable hoverable;

	public NestedVisualNodePart(final VisualNode body, final Set<Tag> tags) {
		super(tags);
		this.body = body;
		body.setParent(createParent());
	}

	protected VisualNodeParent createParent() {
		return new VisualNodeParent() {
			@Override
			public void selectUp(final Context context) {
				select(context);
			}

			@Override
			public Brick createNextBrick(final Context context) {
				if (border != null)
					border.setLast(context, body.getLastBrick(context));
				if (parent == null)
					return null;
				return parent.createNextBrick(context);
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
				border = new BorderAttachment(context,
						context.syntax.hoverStyle,
						body.getFirstBrick(context),
						body.getLastBrick(context)
				);
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
		return body.createFirstBrick(context);
	}

	@Override
	public boolean select(final Context context) {
		if (selected)
			throw new AssertionError("Already selected");
		else if (border != null) {
			context.clearHover();
		}
		selected = true;
		border = new BorderAttachment(context,
				context.syntax.selectStyle,
				body.getFirstBrick(context),
				body.getLastBrick(context)
		);
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
				return getActions(context);
			}
		});
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
	public String debugTreeType() {
		return String.format("nested@%s", Integer.toHexString(hashCode()));
	}

	public String debugTree(final int indent) {
		final String indentString = String.join("", Collections.nCopies(indent, "  "));
		return String.format("%s%s\n%s", indentString, debugTreeType(), body.debugTree(indent + 1));
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
