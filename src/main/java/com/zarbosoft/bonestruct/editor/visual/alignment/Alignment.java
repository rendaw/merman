package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class Alignment {
	public int converse = 0;
	public List<AlignmentListener> listeners = new ArrayList<>();

	public abstract void set(Context context, int converse);

	public void submit(final Context context) {
		for (final AlignmentListener listener : listeners) {
			listener.align(context);
		}
	}

	public abstract void root(Context context, Map<String, Alignment> parents);
}
