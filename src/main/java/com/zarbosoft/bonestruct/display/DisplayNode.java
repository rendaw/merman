package com.zarbosoft.bonestruct.display;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;

public interface DisplayNode {
	int converse(Context context);

	int transverse(Context context);

	int converseSpan(Context context);

	int transverseSpan(Context context);

	void setConverse(Context context, int converse, boolean animate);

	default void setConverse(final Context context, final int converse) {
		setConverse(context, converse, false);
	}

	void setTransverse(Context context, int transverse, boolean animate);

	default void setTransverse(final Context context, final int transverse) {
		setTransverse(context, transverse, false);
	}

	default void setPosition(final Context context, final Vector vector, final boolean animate) {
		setConverse(context, vector.converse, animate);
		setTransverse(context, vector.transverse, animate);
	}

	default int converseEdge(final Context context) {
		return converse(context) + converseSpan(context);
	}

	default int transverseEdge(final Context context) {
		return transverse(context) + transverseSpan(context);
	}
}
