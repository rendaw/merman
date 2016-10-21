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

	@Override
	public Vector end() {
		return new Vector(converseStart + visual.edge().converse, transverseStart);
	}

	@Override
	public Vector edge() {
		return new Vector(converseStart + visual.edge().converse, transverseStart + visual.edge().transverse);
	}

	@Override
	public Vector start() {
		return new Vector(converseStart, transverseStart);
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
	public int startConverse() {
		return converseStart;
	}

	@Override
	public int startTransverse() {
		return transverseStart;
	}

	@Override
	public int startTransverseEdge() {
		return transverseStart + visual.edge().transverse;
	}

	@Override
	public int endConverse() {
		return converseStart + visual.edge().converse;
	}

	@Override
	public int endTransverse() {
		return transverseStart;
	}

	@Override
	public int endTransverseEdge() {
		return transverseStart + visual.edge().transverse;
	}

	@Override
	public void place(final Context context, final Placement placement) {
		if (placement.converseStart != null) {
			converseStart = placement.converseStart;
			final Adjustment parentAdjustment = new Adjustment();
			parentAdjustment.converseEdge = parentAdjustment.converseEnd = edge().converse;
			parent().adjust(context, parentAdjustment);
		}
		if (placement.parentTransverseStart != null) {
			transverseStart = placement.parentTransverseStart;
			final Adjustment parentAdjustment = new Adjustment();
			parentAdjustment.transverseEdge = parentAdjustment.transverseEnd = edge().transverse;
			parent().adjust(context, parentAdjustment);
		}
		final Vector start = new Vector(converseStart, transverseStart);
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
}
