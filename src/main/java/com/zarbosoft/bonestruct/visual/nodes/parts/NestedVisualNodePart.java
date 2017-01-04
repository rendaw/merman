package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Brick;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Hotkeys;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.VisualNodeParent;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class NestedVisualNodePart extends VisualNodePart {
	protected final VisualNode body;
	VisualNodeParent parent;
	boolean selected = false;
	private BorderAttachment borderAttachment;

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
				if (parent == null)
					return null;
				final Brick newLast = parent.createNextBrick(context);
				if (hoverable != null && newLast != null)
					hoverable.borderAttachment.setLast(context, newLast);
				return newLast;
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

			class NestedHoverable extends Context.Hoverable {

				BorderAttachment borderAttachment;

				NestedHoverable(final Context context) {
					borderAttachment = new BorderAttachment(
							context,
							context.syntax.hoverStyle,
							body.getFirstBrick(context),
							body.getLastBrick(context)
					);
				}

				@Override
				public void clear(final Context context) {
					borderAttachment.destroy(context);
					hoverable = null;
				}
			}

			NestedHoverable hoverable;

			@Override
			public Context.Hoverable hover(final Context context) {
				Context.Hoverable parentHoverable = null;
				if (parent != null && (parentHoverable = parent.hover(context)) != null)
					return parentHoverable;
				if (hoverable != null)
					return hoverable;
				hoverable = new NestedHoverable(context);
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
		selected = true;
		/*
		if (border != null && context.hover != null) {
			context.hover = null;
			context.hover.clear(context);
		}
		createBorder(context, context.syntax.select);
		*/
		context.setSelection(context, new Context.Selection() {
			@Override
			public void clear(final Context context) {
				/*
				if (border != null) {
					background.getChildren().remove(0, 1);
					border = null;
				}
				*/
				selected = false;
			}

			@Override
			public Iterable<Context.Action> getActions(final Context context) {
				final Hotkeys hotkeys = context.getHotkeys(tags());
				// TODO update hotkeys/actions on tags change
				return Arrays.asList(new Context.Action() {
					@Override
					public Node buildRule() {
						final com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node node;
						node = hotkeys.hotkeys.get(getName());
						if (node == null)
							return null;
						return node.build();
					}

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
					public Node buildRule() {
						final com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node node;
						node = hotkeys.hotkeys.get(getName());
						if (node == null)
							//return null;
							throw new AssertionError("ja");
						return node.build();
					}

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
				});
			}
		});
		return true;
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
