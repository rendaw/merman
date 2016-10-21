package com.zarbosoft.bonestruct.visual.nodes;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodeParent;

import java.util.Iterator;
import java.util.Map;

public abstract class VisualNode {
	//public Vector start = new Vector(0, 0); // cons absolute, trans relative to preceding/parent

	public abstract void setParent(VisualNodeParent parent);

	public abstract VisualNodeParent parent();

	public abstract Context.Hoverable hover(Context context, Vector point);

	public abstract int startConverse();

	public abstract int startTransverse();

	public abstract int startTransverseEdge();

	public abstract int endConverse();

	public abstract int endTransverse();

	public abstract int endTransverseEdge();

	public static class Placement {
		public Integer converseStart; // Absolute
		public Integer parentTransverseStart; // Relative to parent
		public Integer transverseStartEdge; // Relative to parent
		public Map<String, Alignment> alignments;
	}

	public static class Adjustment {
		public Integer converseEnd; // Absolute
		public Integer converseEdge; // Absolute
		public Integer transverseEnd; // Relative to parent
		public Integer transverseEdge; // Relative to parent

		public boolean isEmpty() {
			return converseEnd == null && converseEdge == null && transverseEnd == null && transverseEdge == null;
		}
	}

	public abstract void place(Context context, final Placement placement);

	public abstract Layer visual();

	public Iterator<VisualNode> children() {
		return Iterators.forArray();
	}

	public abstract void compact(Context context);

	public abstract Vector end(); // cons absolute, trans relative to parent

	public abstract Vector edge(); // cons absolute, trans relative to parent

	public abstract Vector start();

	public boolean isIn(final Vector point) {
		return Obbox.isIn(
				startConverse(),
				startTransverse(),
				startTransverseEdge(),
				endConverse(),
				endTransverse(),
				endTransverseEdge(),
				point
		);
	}
}
