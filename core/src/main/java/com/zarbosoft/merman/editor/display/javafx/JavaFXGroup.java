package com.zarbosoft.merman.editor.display.javafx;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import javafx.scene.Node;

public class JavaFXGroup extends JavaFXNode implements Group {
	final OverlayList<Node> children;
	final javafx.scene.Group group = new javafx.scene.Group();
	private final JavaFXDisplay display;

	public JavaFXGroup(final JavaFXDisplay display) {
		children = new OverlayList<>(group.getChildren());
		this.display = display;
	}

	@Override
	public void add(
			final int index, final DisplayNode node
	) {
		children.add(index, ImmutableList.of(((JavaFXNode) node).node()));
		display.dirty.add(this);
	}

	@Override
	public void addAll(final int index, final ImmutableList<DisplayNode> nodes) {
		children.add(index,
				nodes.stream().map(node -> ((JavaFXNode) node).node()).collect(ImmutableList.toImmutableList())
		);
		display.dirty.add(this);
	}

	@Override
	public void remove(final int index, final int count) {
		children.remove(index, count);
		display.dirty.add(this);
	}

	@Override
	public void remove(final DisplayNode node) {
		children.remove(((JavaFXNode) node).node());
		display.dirty.add(this);
	}

	@Override
	public int size() {
		return children.size();
	}

	@Override
	public void clear() {
		children.clear();
		display.dirty.add(this);
	}

	@Override
	protected Node node() {
		return group;
	}

	@Override
	public void flush() {
		children.flush();
	}
}
