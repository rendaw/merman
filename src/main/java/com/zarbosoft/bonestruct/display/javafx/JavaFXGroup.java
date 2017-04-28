package com.zarbosoft.bonestruct.display.javafx;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Group;
import javafx.scene.Node;

import java.util.List;
import java.util.stream.Collectors;

public class JavaFXGroup extends JavaFXNode implements Group {
	javafx.scene.Group group = new javafx.scene.Group();

	@Override
	public void add(
			final int index, final DisplayNode node
	) {
		group.getChildren().add(index, ((JavaFXNode) node).node());
	}

	@Override
	public void addAll(final int index, final List<DisplayNode> nodes) {
		group
				.getChildren()
				.addAll(index, nodes.stream().map(node -> ((JavaFXNode) node).node()).collect(Collectors.toList()));
	}

	@Override
	public void remove(final int index, final int count) {
		group.getChildren().subList(index, index + count).clear();
	}

	@Override
	public void remove(final DisplayNode node) {
		group.getChildren().remove(((JavaFXNode) node).node());
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	protected Node node() {
		return group;
	}
}
