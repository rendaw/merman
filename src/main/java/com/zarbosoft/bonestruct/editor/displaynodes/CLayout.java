package com.zarbosoft.bonestruct.editor.displaynodes;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Group;
import com.zarbosoft.bonestruct.editor.Context;

import java.util.ArrayList;
import java.util.List;

public class CLayout {
	private final Group group;
	List<DisplayNode> nodes = new ArrayList<>();

	public CLayout(final Group group) {
		this.group = group;
	}

	public void add(final DisplayNode node) {
		group.add(node);
	}

	public void layout(final Context context) {
		int converse = 0;
		for (final DisplayNode node : nodes) {
			node.setConverse(context, converse, false);
			converse += node.converseSpan(context);
		}
	}
}
