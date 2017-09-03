package com.zarbosoft.merman.editor.display.derived;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.visual.Vector;

import java.util.ArrayList;
import java.util.List;

public class ColumnarTableLayout {
	public final Group group;
	private final int maxTransverse;
	int columns;
	List<List<DisplayNode>> rows = new ArrayList<>();

	public ColumnarTableLayout(final Display display, final int maxTransverse) {
		this.group = display.group();
		this.maxTransverse = maxTransverse;
	}

	public ColumnarTableLayout(final Context context, final int maxTransverse) {
		this(context.display, maxTransverse);
	}

	public void add(final List<DisplayNode> row) {
		columns = Math.max(columns, row.size());
		rows.add(row);
		for (final DisplayNode node : row)
			group.add(node);
	}

	public void layout(final Context context) {
		int start = 0;
		final int converseSum = 0;
		int columnEdge = 0;
		while (start < rows.size()) {
			int newColumnEdge = 0;
			int end = start;

			// Find the converse size of each column, and the number of rows that fit in the transverse span
			final int[] columnSpans = new int[columns];
			final List<Integer> rowStarts = new ArrayList<>();
			{
				int transverse = 0;
				for (int y = start; y < rows.size(); ++y) {
					final List<DisplayNode> row = rows.get(y);
					int rowTransverseSpan = 0;
					for (int x = 0; x < columns; ++x) {
						final DisplayNode cell = row.get(x);
						rowTransverseSpan = Math.max(rowTransverseSpan, cell.transverseSpan(context));
					}
					if (end > start && transverse + rowTransverseSpan >= maxTransverse) {
						break;
					}
					for (int x = 0; x < columns; ++x) {
						final DisplayNode cell = row.get(x);
						columnSpans[x] = Math.max(columnSpans[x], cell.converseSpan(context));
					}
					rowStarts.add(transverse);
					transverse += rowTransverseSpan;
					end += 1;
				}
			}

			// Place everything
			for (int y = start; y < end; ++y) {
				final List<DisplayNode> row = rows.get(y);
				int converse = columnEdge;
				for (int x = 0; x < columns; ++x) {
					final DisplayNode cell = row.get(x);
					int transverse = rowStarts.get(y - start);
					if (cell instanceof Text)
						transverse += ((Text) cell).font().getAscent();
					cell.setPosition(context, new Vector(converse, transverse), false);
					converse += columnSpans[x];
				}
				newColumnEdge = Math.max(newColumnEdge, converse);
			}

			//
			start = end;
			columnEdge = newColumnEdge;
		}
	}
}
