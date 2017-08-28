package com.zarbosoft.bonestruct.editor.display.derived;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Group;

import java.util.ArrayList;
import java.util.List;

public class TLayout {
	private final Group group;
	List<DisplayNode> nodes = new ArrayList<>();

	public TLayout(final Group group) {
		this.group = group;
	}

	public void add(final DisplayNode node) {
		group.add(node);
	}

	public void layout(final Context context) {
		int transverse = 0;
		for (final DisplayNode node : nodes) {
			node.setTransverse(context, transverse, false);
			transverse += node.transverseSpan(context);
		}
	}
}
