package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.syntax.style.ModelColor;

import java.util.function.Consumer;

public interface Display {
	Group group();

	Image image();

	Text text();

	Font font(String font, int fontSize);

	Drawing drawing();

	Blank blank();

	void addMouseExitListener(Runnable listener);

	void addMouseMoveListener(Consumer<Vector> listener);

	void addHIDEventListener(Consumer<HIDEvent> listener);

	void addTypingListener(Consumer<String> listener);

	void focus();

	@FunctionalInterface
	interface IntListener {
		void changed(int oldValue, int newValue);
	}

	int edge(Context context);

	void addConverseEdgeListener(IntListener listener);

	void removeConverseEdgeListener(IntListener listener);

	int transverseEdge(Context context);

	void addTransverseEdgeListener(IntListener listener);

	void removeTransverseEdgeListener(IntListener listener);

	void add(int index, DisplayNode node);

	default void add(final DisplayNode node) {
		add(size(), node);
	}

	int size();

	void remove(DisplayNode node);

	void setBackgroundColor(ModelColor color);

	void flush();
}
