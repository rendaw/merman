package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MockeryGroup extends MockeryDisplayNode implements Group {
	List<MockeryDisplayNode> nodes = new ArrayList<>();

	@Override
	public void add(final int index, final DisplayNode node) {
		nodes.add(index, (MockeryDisplayNode) node);
	}

	@Override
	public void addAll(final int index, final List<DisplayNode> nodes) {
		this.nodes.addAll(index, nodes.stream().map(node -> (MockeryDisplayNode) node).collect(Collectors.toList()));
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

	public int count() {
		return nodes.size();
	}

	public MockeryDisplayNode get(final int index) {
		return nodes.get(index);
	}
}
