package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodeParent;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;
import javafx.scene.Group;

@Luxem.Configuration(name = "pad")
public class FrontSpace implements FrontConstantPart {
	@Luxem.Configuration(optional = true)
	public FrontMark.Style style = new FrontMark.Style();
	@Luxem.Configuration
	public int size;

	@Override
	public VisualNodePart createVisual(final Context context) {
		return new VisualNodePart() {
			public VisualNodeParent parent;
			int startConverse = 0;
			int startTransverse = 0;
			Layer visual = new Layer(new Group(), new Group());

			@Override
			public Break breakMode() {
				return style.breakMode;
			}

			@Override
			public String alignmentName() {
				return style.alignment;
			}

			@Override
			public String alignmentNameCompact() {
				return style.alignmentCompact;
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
				return startConverse;
			}

			@Override
			public int startTransverse(final Context context) {
				return startTransverse;
			}

			@Override
			public int startTransverseEdge(final Context context) {
				return startTransverse + context.syntax.lineSpan;
			}

			@Override
			public int endConverse(final Context context) {
				return edge(context);
			}

			@Override
			public int endTransverse(final Context context) {
				return startTransverse;
			}

			@Override
			public int endTransverseEdge(final Context context) {
				return startTransverse + context.syntax.lineSpan;
			}

			@Override
			public int edge(final Context context) {
				return Math.min(context.edge, startConverse + size);
			}

			@Override
			public void place(final Context context, final Placement placement) {
				if (placement.converseStart != null)
					startConverse = placement.converseStart;
				if (placement.parentTransverseStart != null)
					startConverse = placement.parentTransverseStart;
			}

			@Override
			public Layer visual() {
				return visual;
			}

			@Override
			public void compact(final Context context) {

			}
		};
	}
}
