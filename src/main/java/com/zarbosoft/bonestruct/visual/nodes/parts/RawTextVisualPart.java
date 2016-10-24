package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.nodes.Layer;

public abstract class RawTextVisualPart extends VisualNodePart {
	public VisualNodeParent parent;
	public RawText visual;
	int converseStart = 0;
	int transverseStart = 0;

	public RawTextVisualPart(final Context context) {
		visual = RawText.create(context);
	}

	public void setText(final String value) {
		visual.setText(value);
	}

	public String getText() {
		return visual.getText();
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
		return null;
	}

	@Override
	public int startConverse(final Context context) {
		return converseStart;
	}

	@Override
	public int startTransverse(final Context context) {
		return transverseStart;
	}

	@Override
	public int startTransverseEdge(final Context context) {
		return transverseStart + context.syntax.lineSpan;
	}

	@Override
	public int endConverse(final Context context) {
		return converseStart + visual.edge().converse;
	}

	@Override
	public int endTransverse(final Context context) {
		return transverseStart;
	}

	@Override
	public int endTransverseEdge(final Context context) {
		return transverseStart + context.syntax.lineSpan;
	}

	@Override
	public int edge(final Context context) {
		return converseStart + visual.edge().converse;
	}

	@Override
	public void place(final Context context, final Placement placement) {
		if (placement.converseStart != null) {
			converseStart = placement.converseStart;
			final Adjustment parentAdjustment = new Adjustment();
			parentAdjustment.converseEnd = endConverse(context);
			parentAdjustment.converseEdge = edge(context);
			parent().adjust(context, parentAdjustment);
		}
		if (placement.parentTransverseStart != null) {
			transverseStart = placement.parentTransverseStart;
			final Adjustment parentAdjustment = new Adjustment();
			parentAdjustment.transverseEnd = transverseStart;
			parentAdjustment.transverseEdge = transverseStart + context.syntax.lineSpan;
			parent().adjust(context, parentAdjustment);
		}
		final Vector start = new Vector(converseStart, transverseStart + context.syntax.lineSpan / 2);
		/*
		System.out.println(String.format("Mark [%s] to %s", visual.getText(), start));
		*/
		context.translate(visual, start);
	}

	@Override
	public Layer visual() {
		return new Layer(visual, null);
	}

	@Override
	public void compact(final Context context) {
		// nop
	}

	@Override
	public String debugTreeType() {
		return String.format("raw@%s %s", Integer.toHexString(hashCode()), visual.getText());
	}
}
