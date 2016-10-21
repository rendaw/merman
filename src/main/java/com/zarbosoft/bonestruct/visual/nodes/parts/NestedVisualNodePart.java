package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.Obbox;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import javafx.scene.layout.StackPane;

public abstract class NestedVisualNodePart extends VisualNodePart {
	private final VisualNode body;
	StackPane background = new StackPane();
	Obbox border = null;
	VisualNodeParent parent;

	public NestedVisualNodePart(final VisualNode body) {
		this.body = body;
		background.getChildren().add(body.visual().background);
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
							body.startConverse(),
							body.startTransverse(),
							body.startTransverseEdge(),
							body.endConverse(),
							body.endTransverse(),
							body.endTransverseEdge()
					);
				}
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
				body.startConverse(),
				body.startTransverse(),
				body.startTransverseEdge(),
				body.endConverse(),
				body.endTransverse(),
				body.endTransverseEdge(),
				point
		)) {
			return new Hoverable();
		}
		return null;
	}

	@Override
	public int startConverse() {
		return body.startConverse();
	}

	@Override
	public int startTransverse() {
		return body.startTransverse();
	}

	@Override
	public int startTransverseEdge() {
		return body.startTransverseEdge();
	}

	@Override
	public int endConverse() {
		return body.endConverse();
	}

	@Override
	public int endTransverse() {
		return body.endTransverse();
	}

	@Override
	public int endTransverseEdge() {
		return body.endTransverseEdge();
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
	public Vector end() {
		return body.end();
	}

	@Override
	public Vector edge() {
		return body.edge();
	}

	@Override
	public Vector start() {
		return body.start();
	}

	@Override
	public void compact(final Context context) {
		// nop
	}

	private class Hoverable extends Context.Hoverable {
		@Override
		public Context.Hoverable hover(final Context context, final Vector point) {
			if (Obbox.isIn(
					body.startConverse(),
					body.startTransverse(),
					body.startTransverseEdge(),
					body.endConverse(),
					body.endTransverse(),
					body.endTransverseEdge(),
					point
			)) {
				final Context.Hoverable out = body.hover(context, point);
				if (out == null) {
					if (border == null) {
						border = Obbox.fromSettings(context.syntax.hover);
						border.setSize(
								context,
								body.startConverse(),
								body.startTransverse(),
								body.startTransverseEdge(),
								body.endConverse(),
								body.endTransverse(),
								body.endTransverseEdge()
						);
						background.getChildren().add(0, border);
					}
					return this;
				} else
					return out;
			} else
				return parent.hoverUp();
		}

		@Override
		public void clear(final Context context) {
			if (border != null) {
				background.getChildren().remove(0, 1);
				border = null;
			}
		}
	}
}
