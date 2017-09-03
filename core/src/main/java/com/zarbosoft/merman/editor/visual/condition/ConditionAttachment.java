package com.zarbosoft.merman.editor.visual.condition;

import com.zarbosoft.merman.editor.Context;

import java.util.ArrayList;
import java.util.List;

public abstract class ConditionAttachment {
	private boolean show = false;
	private final List<Listener> listeners = new ArrayList<>();
	private final boolean invert;

	protected ConditionAttachment(final boolean invert) {
		this.invert = invert;
	}

	public interface Listener {
		void conditionChanged(Context context, boolean show);
	}

	public void register(final Listener listener) {
		listeners.add(listener);
	}

	public boolean show() {
		return show;
	}

	public abstract void destroy(Context context);

	public void setState(final Context context, final boolean show) {
		if (this.show == show)
			return;
		this.show = show;
		listeners.forEach(listener -> listener.conditionChanged(context, show));
	}
}
