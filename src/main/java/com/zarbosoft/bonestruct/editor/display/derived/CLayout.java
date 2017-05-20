package com.zarbosoft.bonestruct.editor.display.derived;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.Display;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Group;

import java.util.ArrayList;
import java.util.List;

public class CLayout {
	public final Group group;
	List<DisplayNode> nodes = new ArrayList<>();

	public CLayout(final Group group) {
		this.group = group;
	}

	public CLayout(final Display display) {
		this.group = display.group();
	}

	public void add(final DisplayNode node) {
		group.add(node);
		nodes.add(node);
	}

	public void layout(final Context context) {
		int converse = 0;
		for (final DisplayNode node : nodes) {
			node.setConverse(context, converse, false);
			converse += node.converseSpan(context);
		}
	}
}
