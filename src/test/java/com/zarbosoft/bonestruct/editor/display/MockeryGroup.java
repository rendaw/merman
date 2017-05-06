package com.zarbosoft.bonestruct.editor.display;

import com.zarbosoft.bonestruct.editor.Context;

import java.util.ArrayList;
import java.util.List;

public class MockeryGroup extends MockeryDisplayNode implements Group {
	List<DisplayNode> nodes = new ArrayList<>();

	@Override
	public void add(final int index, final DisplayNode node) {
		nodes.add(index, node);
	}

	@Override
	public void addAll(final int index, final List<DisplayNode> nodes) {
		this.nodes.addAll(index, nodes);
	}

	@Override
	public void remove(final int index, final int count) {
		nodes.subList(index, index + count - 1).clear();
	}

	@Override
	public void remove(final DisplayNode node) {
		nodes.remove(node);
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public void clear() {
		nodes.clear();
	}

	@Override
	public int converseSpan(final Context context) {
		return nodes.stream().mapToInt(n -> n.converseEdge(context)).max().orElse(0);
	}

	@Override
	public int transverseSpan(final Context context) {
		return nodes.stream().mapToInt(n -> n.transverseEdge(context)).max().orElse(0);
	}
}
