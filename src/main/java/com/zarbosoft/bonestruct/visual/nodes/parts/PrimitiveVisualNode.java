package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.alignment.RelativeAlignment;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.Obbox;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.layout.StackPane;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class PrimitiveVisualNode extends GroupVisualNode {
	private final ChangeListener<String> dataListener;
	private final StackPane background = new StackPane();
	private Obbox border = null;
	private VisualNodeParent parent;

	public PrimitiveVisualNode(final Context context, final StringProperty data) {
		background.getChildren().add(super.visual().background);
		alignments.put("soft", new RelativeAlignment(alignment(), softIndent()));
		dataListener = new ChangeListener<String>() {
			@Override
			public void changed(
					final ObservableValue<? extends String> observable, final String oldValue, final String newValue
			) {
				// TODO change binding so Context is passed in
				// TODO make more efficient, don't recreate all lines
				// TODO actually, the data should be a list of string properties (per hard line) rather than a single
				// and at the top level it should be a list binding
				removeAll(context);
				hardLines.clear();
				boolean first = true;
				for (final String split : newValue.split("\n")) {
					final HardLine hardLine = new HardLine(first);
					first = false;
					final Line line = new Line(context, true);
					line.setText(split);
					hardLine.lines.add(line);
					hardLine.add(context, line, -1);
					hardLines.add(hardLine);
					add(context, hardLine, -1);
				}
			}
		};
		data.addListener(new WeakChangeListener<>(dataListener));
		dataListener.changed(null, null, data.getValue());
	}

	protected abstract int softIndent();

	protected abstract boolean breakFirst();

	protected abstract boolean level();

	protected abstract String alignment();

	@Override
	public Break breakMode() {
		return Break.NEVER;
	}

	@Override
	public String alignmentName() {
		return null;
	}

	@Override
	public String alignmentNameCompact() {
		return null;
	}

	@Override
	public Layer visual() {
		return new Layer(super.visual().foreground, background);
	}

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
		super.parent = new VisualNodeParent() {
			@Override
			public void adjust(final Context context, final VisualNode.Adjustment adjustment) {
				if (border != null) {
					border.setSize(
							context,
							startConverse(),
							startTransverse(),
							startTransverseEdge(),
							endConverse(),
							endTransverse(),
							endTransverseEdge()
					);
				}
				parent.adjust(context, adjustment);
			}

			@Override
			public VisualNodeParent parent() {
				return parent;
			}

			@Override
			public VisualNodePart target() {
				return PrimitiveVisualNode.this;
			}

			@Override
			public void align(final Context context) {
				parent.align(context);
			}

			@Override
			public Context.Hoverable hoverUp() {
				return new Hoverable();
			}
		};
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	class HardLine extends GroupVisualNode {
		List<Line> lines = new ArrayList<>();

		final boolean first;

		HardLine(final boolean first) {
			this.first = first;
		}

		@Override
		public Break breakMode() {
			return (!first || breakFirst() && hardLines.size() > 1) ? Break.ALWAYS : Break.COMPACT;
		}

		@Override
		public String alignmentName() {
			return (!first || level()) ? alignment() : null;
		}

		@Override
		public String alignmentNameCompact() {
			return alignment();
		}
	}

	class Line extends RawTextVisualPart {
		String text;
		private final boolean hard;

		Line(final Context context, final boolean hard) {
			super(context);
			this.hard = hard;
		}

		@Override
		public Break breakMode() {
			return hard ? Break.NEVER : Break.ALWAYS;
		}

		@Override
		public String alignmentName() {
			return hard ? null : "soft";
		}

		@Override
		public String alignmentNameCompact() {
			return hard ? null : "soft";
		}
	}

	final List<HardLine> hardLines = new ArrayList<>();

	public Context.Hoverable hover(final Context context, final Vector point) {
		if (isIn(point)) {
			return new Hoverable();
		}
		return null;
	}

	private class Hoverable extends Context.Hoverable {
		@Override
		public Context.Hoverable hover(final Context context, final Vector point) {
			if (isIn(point)) {
				if (border == null) {
					border = Obbox.fromSettings(context.syntax.hover);
					border.setSize(
							context,
							startConverse(),
							startTransverse(),
							startTransverseEdge(),
							endConverse(),
							endTransverse(),
							endTransverseEdge()
					);
					background.getChildren().add(0, border);
				}
				return this;
			} else
				return parent().hoverUp();
		}

		@Override
		public void clear(final Context context) {
			if (border != null) {
				background.getChildren().remove(0, 1);
				border = null;
			}
		}
	}

	@Override
	public void compact(final Context context) {
		final StringBuilder buffer = new StringBuilder();
		for (final HardLine hardLine : hardLines) {
			final BreakIterator breakIterator = BreakIterator.getLineInstance();
			Line line;
			for (final Iterator<Line> lineIter = hardLine.lines.iterator(); lineIter.hasNext(); ) {
				line = lineIter.next();
				if (buffer.length() > 0) {
					line.setText(buffer.toString() + line.text);
					buffer.delete(0, buffer.length());
				}
				if (line.end().converse > context.edge) {
					breakIterator.setText(line.text);
					final int breakAt = breakIterator.preceding(line.visual.getUnder(context.edge));
					if (breakAt > 0) {
						buffer.append(line.text.substring(breakAt, -1));
						line.setText(line.text.substring(0, breakAt));
					}
				}
			}
			while (buffer.length() > 0) {
				line = new Line(context, false);
				line.setText(buffer.toString());
				buffer.delete(0, buffer.length());
				if (line.end().converse > context.edge) {
					breakIterator.setText(line.text);
					final int breakAt = breakIterator.preceding(line.visual.getUnder(context.edge));
					buffer.append(line.text.substring(breakAt, -1));
					line.setText(line.text.substring(0, breakAt));
				}
				hardLine.lines.add(line);
				hardLine.add(context, line, -1);
			}
		}
	}
}
