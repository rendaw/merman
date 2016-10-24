package com.zarbosoft.bonestruct.visual.nodes;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodeParent;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public abstract class VisualNode {
	//public Vector start = new Vector(0, 0); // cons absolute, trans relative to preceding/parent

	public abstract void setParent(VisualNodeParent parent);

	public abstract VisualNodeParent parent();

	public abstract Context.Hoverable hover(Context context, Vector point);

	public abstract int startConverse(Context context);

	public abstract int startTransverse(Context context);

	public abstract int startTransverseEdge(Context context);

	public abstract int endConverse(Context context);

	public abstract int endTransverse(Context context);

	public abstract int endTransverseEdge(Context context);

	public abstract int edge(Context context);

	public static class Placement {
		public Integer converseStart; // Absolute
		public Integer parentTransverseStart; // Relative to parent
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

	public boolean isIn(final Context context, final Vector point) {
		return Obbox.isIn(
				startConverse(context),
				startTransverse(context),
				startTransverseEdge(context),
				endConverse(context),
				endTransverse(context),
				endTransverseEdge(context),
				point
		);
	}

	public String debugTreeType() {
		return toString();
	}

	public String debugTree(final int indent) {
		final String indentString = String.join("", Collections.nCopies(indent, "  "));
		return String.format("%s%s", indentString, debugTreeType());
	}
}
