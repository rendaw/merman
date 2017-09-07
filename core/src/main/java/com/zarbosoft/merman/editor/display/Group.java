package com.zarbosoft.merman.editor.display;

import com.google.common.collect.ImmutableList;

public interface Group extends DisplayNode {
	void add(int index, DisplayNode node);

	default void add(final DisplayNode node) {
		add(size(), node);
	}

	void addAll(int index, ImmutableList<DisplayNode> nodes);

	default void remove(final int index) {
		remove(index, 1);
	}

	void remove(int index, int count);

	void remove(DisplayNode node);

	int size();

	void clear();

}
