package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.Obbox;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.pidgoon.internal.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public abstract class NestedVisualNodePart extends VisualNodePart {
	private final VisualNode body;
	StackPane background = new StackPane();
	Obbox border = null;
	VisualNodeParent parent;
	boolean selected = false;

	public NestedVisualNodePart(final VisualNode body) {
		this.body = body;
		body.setParent(new VisualNodeParent() {

			@Override
			public void adjust(final Context context, final VisualNode.Adjustment adjustment) {
				if (border != null) {
					border.setSize(
							context,
							body.startConverse(context),
							body.startTransverse(context),
							body.startTransverseEdge(context),
							body.endConverse(context),
							body.endTransverse(context),
							body.endTransverseEdge(context)
					);
				}
				if (NestedVisualNodePart.this.parent != null)
					NestedVisualNodePart.this.parent.adjust(context, adjustment);
			}

			@Override
			public VisualNodeParent parent() {
				return NestedVisualNodePart.this.parent;
			}

			@Override
			public VisualNodePart target() {
				return NestedVisualNodePart.this;
			}

			@Override
			public void align(final Context context) {
				if (NestedVisualNodePart.this.parent != null)
					NestedVisualNodePart.this.parent.align(context);
			}

			@Override
			public Context.Hoverable hoverUp(final Context context) {
				return new Hoverable();
			}

			@Override
			public void selectUp(final Context context) {
				select(context);
			}
		});
		final Pane temp = new Pane();
		temp.getChildren().add(body.visual().background);
		background.getChildren().add(temp);
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
	public Context.Hoverable hover(final Context context, final Vector point) {
		if (selected) {
			return body.hover(context, point);
		} else {
			if (Obbox.isIn(
					body.startConverse(context),
					body.startTransverse(context),
					body.startTransverseEdge(context),
					body.endConverse(context),
					body.endTransverse(context),
					body.endTransverseEdge(context),
					point
			)) {
				return new Hoverable();
			}
			return null;
		}
	}

	private void createBorder(final Context context, final Obbox.Settings settings) {
		border = Obbox.fromSettings(settings);
		border.setSize(
				context,
				body.startConverse(context),
				body.startTransverse(context),
				body.startTransverseEdge(context),
				body.endConverse(context),
				body.endTransverse(context),
				body.endTransverseEdge(context)
		);
		final Pane temp = new Pane();
		temp.getChildren().add(border);
		background.getChildren().add(0, temp);
	}

	@Override
	public boolean select(final Context context) {
		if (selected)
			throw new AssertionError("Already selected");
		selected = true;
		if (border != null && context.hover != null) {
			context.hover = null;
			context.hover.clear(context);
		}
		createBorder(context, context.syntax.select);
		context.setSelection(context, new Context.Selection() {
			@Override
			public void clear(final Context context) {
				if (border != null) {
					background.getChildren().remove(0, 1);
					border = null;
				}
				selected = false;
			}

			@Override
			public Iterable<Context.Action> getActions(final Context context) {
				return Arrays.asList(new Context.Action() {
					@Override
					public Node buildRule() {
						com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node node;
						node = getHotkeys(context).get(getName());
						if (node == null)
							node = context.syntax.hotkeys.get(getName());
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
						com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node node;
						node = getHotkeys(context).get(getName());
						if (node == null)
							node = context.syntax.hotkeys.get(getName());
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
	public int startConverse(final Context context) {
		return body.startConverse(context);
	}

	@Override
	public int startTransverse(final Context context) {
		return body.startTransverse(context);
	}

	@Override
	public int startTransverseEdge(final Context context) {
		return body.startTransverseEdge(context);
	}

	@Override
	public int endConverse(final Context context) {
		return body.endConverse(context);
	}

	@Override
	public int endTransverse(final Context context) {
		return body.endTransverse(context);
	}

	@Override
	public int endTransverseEdge(final Context context) {
		return body.endTransverseEdge(context);
	}

	@Override
	public int edge(final Context context) {
		return body.edge(context);
	}

	@Override
	public void place(final Context context, final VisualNode.Placement placement) {
		body.place(context, placement);
	}

	@Override
	public Layer visual() {
		return new Layer(body.visual().foreground, background);
	}

	@Override
	public void compact(final Context context) {
		// nop
	}

	private class Hoverable extends Context.Hoverable {
		@Override
		public Context.Hoverable hover(final Context context, final Vector point) {
			if (Obbox.isIn(
					body.startConverse(context),
					body.startTransverse(context),
					body.startTransverseEdge(context),
					body.endConverse(context),
					body.endTransverse(context),
					body.endTransverseEdge(context),
					point
			)) {
				final Context.Hoverable out = body.hover(context, point);
				if (out == null) {
					if (border == null)
						createBorder(context, context.syntax.hover);
					return this;
				} else
					return out;
			} else if (parent != null)
				return parent.hoverUp(context);
			else
				return null;
		}

		@Override
		public void clear(final Context context) {
			if (border != null) {
				background.getChildren().remove(0, 1);
				border = null;
			}
		}
	}

	public abstract Map<String, com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node> getHotkeys(Context contex);

	@Override
	public String debugTreeType() {
		return String.format("nested@%s", Integer.toHexString(hashCode()));
	}

	public String debugTree(final int indent) {
		final String indentString = String.join("", Collections.nCopies(indent, "  "));
		return String.format("%s%s\n%s", indentString, debugTreeType(), body.debugTree(indent + 1));
	}
}
