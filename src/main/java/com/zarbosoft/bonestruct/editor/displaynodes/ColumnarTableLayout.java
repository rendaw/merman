package com.zarbosoft.bonestruct.editor.displaynodes;

import com.zarbosoft.bonestruct.display.DisplayNode;
import com.zarbosoft.bonestruct.display.Group;
import com.zarbosoft.bonestruct.editor.Context;

import java.util.ArrayList;
import java.util.List;

public class ColumnarTableLayout {
	public final Group group;
	private final int maxTransverse;
	int columns;
	List<List<DisplayNode>> cells = new ArrayList<>();

	public ColumnarTableLayout(final Context context, final int maxTransverse) {
		this.group = context.display.group();
		this.maxTransverse = maxTransverse;
	}

	public void add(final List<DisplayNode> row) {
		columns = Math.max(columns, row.size());
		cells.add(row);
		for (final DisplayNode node : row)
			group.add(node);
	}

	public void layout(final Context context) {
		int start = 0;
		int converseSum = 0;
		while (start < cells.size()) {
			int end = start;
			final List<Integer> spans = new ArrayList<>();
			{
				int transverse = 0;
				for (int y = start; y < cells.size(); ++y) {
					final List<DisplayNode> row = cells.get(y);
					final int span = row.stream().mapToInt(node -> node.converseSpan(context)).max().orElse(0);
					spans.add(span);
					transverse += span;
					if (transverse >= maxTransverse) {
						break;
					}
					end += 1;
				}
			}
			int transverse = 0;
			for (int x = 0; x < columns; ++x) {
				int converse = 0;
				for (int y = start; y < end; ++y) {
					final List<DisplayNode> row = cells.get(y);
					final int span = spans.get(y - start);
					if (x < row.size()) {
						final DisplayNode cell = row.get(x);
						cell.setTransverse(context, transverse);
						cell.setConverse(context, converseSum);
						converse = Math.max(converse, cell.converseSpan(context));
					}
					transverse += span;
				}
				converseSum += converse;
			}
			start = end;
		}
	}
}
