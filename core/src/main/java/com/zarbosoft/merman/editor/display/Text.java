package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.syntax.style.ModelColor;

public interface Text extends DisplayNode {
	String text();

	void setText(Context context, String text);

	void setColor(Context context, ModelColor color);

	Font font();

	void setFont(Context context, Font font);

	int getIndexAtConverse(final Context context, final int converse);

	int getConverseAtIndex(final int index);

	@Override
	default int transverseEdge(final Context context) {
		return transverse(context) + font().getDescent();
	}
}
