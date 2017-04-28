package com.zarbosoft.bonestruct.display;

import java.util.List;

public interface Group extends DisplayNode {
	void add(int index, DisplayNode node);

	default void add(final DisplayNode node) {
		add(size(), node);
	}

	void addAll(int index, List<DisplayNode> nodes);

	default void remove(final int index) {
		remove(index, 1);
	}

	void remove(int index, int count);

	void remove(DisplayNode node);

	int size();

	void clear();

}
