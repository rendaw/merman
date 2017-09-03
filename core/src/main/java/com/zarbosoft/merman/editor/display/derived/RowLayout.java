package com.zarbosoft.merman.editor.display.derived;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;

import java.util.ArrayList;
import java.util.List;

public class RowLayout {
	public final Group group;
	List<DisplayNode> nodes = new ArrayList<>();

	public RowLayout(final Context context) {
		this(context.display);
	}

	public RowLayout(final Display display) {
		this.group = display.group();
	}

	public void add(final DisplayNode node) {
		group.add(node);
		nodes.add(node);
	}

	public void layout(final Context context) {
		int converse = 0;
		int maxAscent = 0;
		for (final DisplayNode node : nodes) {
			if (node instanceof Text)
				maxAscent = Math.max(maxAscent, ((Text) node).font().getAscent());
		}
		for (final DisplayNode node : nodes) {
			if (node instanceof Text) {
				node.setTransverse(context, maxAscent);
			}
			node.setConverse(context, converse, false);
			converse += node.converseSpan(context);
		}
	}
}
