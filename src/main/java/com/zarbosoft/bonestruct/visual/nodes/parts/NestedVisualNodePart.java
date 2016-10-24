package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.Obbox;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.Collections;

public abstract class NestedVisualNodePart extends VisualNodePart {
	private final VisualNode body;
	StackPane background = new StackPane();
	Obbox border = null;
	VisualNodeParent parent;

	public NestedVisualNodePart(final VisualNode body) {
		this.body = body;
		final Pane temp = new Pane();
		temp.getChildren().add(body.visual().background);
		background.getChildren().add(temp);
	}

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
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
			public Context.Hoverable hoverUp() {
				return new Hoverable();
			}
		});
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

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
			return new Hoverable();
		}
		return null;
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
					if (border == null) {
						border = Obbox.fromSettings(context.syntax.hover);
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
					return this;
				} else
					return out;
			} else if (parent != null)
				return parent.hoverUp();
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

	@Override
	public String debugTreeType() {
		return String.format("nested@%s", Integer.toHexString(hashCode()));
	}

	public String debugTree(final int indent) {
		final String indentString = String.join("", Collections.nCopies(indent, "  "));
		return String.format("%s%s\n%s", indentString, debugTreeType(), body.debugTree(indent + 1));
	}
}
