package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.ObboxStyle;
import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.CursorAttachment;
import com.zarbosoft.bonestruct.editor.visual.bricks.TextBrick;
import com.zarbosoft.bonestruct.editor.visual.raw.Obbox;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PrimitiveVisualNode extends VisualNodePart {
	// INVARIANT: Leaf nodes must always create at least one brick
	private final ChangeListener<String> dataListener;
	private final Obbox border = null;
	private VisualNodeParent parent;
	Alignment softAlignment, hardAlignment, firstAlignment;
	Style.Baked softStyle, hardStyle, firstStyle;
	Set<Tag> softTags = new HashSet<>(), hardTags = new HashSet<>();
	int brickCount = 0;
	private PrimitiveHoverable hoverable;

	private void getStyles(final Context context) {
		final PSet<Tag> tags = HashTreePSet.from(tags());
		firstStyle = context.getStyle(tags.plus(new StateTag("hard")).plus(new StateTag("first")));
		hardStyle = context.getStyle(tags.plus(new StateTag("hard")));
		softStyle = context.getStyle(tags.plus(new StateTag("soft")));
		firstAlignment = getAlignment(firstStyle.alignment);
		hardAlignment = getAlignment(hardStyle.alignment);
		softAlignment = getAlignment(softStyle.alignment);
	}

	@Override
	public void changeTags(final Context context, final TagsChange tagsChange) {
		super.changeTags(context, tagsChange);
		final boolean fetched = false;
		if (brickCount == 0)
			return;
		getStyles(context);
		for (final Line line : lines) {
			if (line.brick == null)
				continue;
			line.brick.changed(context);
		}
	}

	@Override
	public boolean select(final Context context) {
		return false; // TODO
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		if (lines.isEmpty())
			return null;
		return lines.get(0).brick;
	}

	@Override
	public Brick getLastBrick(final Context context) {
		if (lines.isEmpty())
			return null;
		return Helper.last(lines).brick;
	}

	@Override
	public Context.Hoverable hover(final Context context, final Vector point) {
		if (parent != null) {
			return parent.hover(context, point);
		}
		return null;
	}

	protected Iterable<Context.Action> getActions(final Context context) {
		return ImmutableList.of(); // TODO
	}

	private class PrimitiveHoverable extends Context.Hoverable {
		final CursorAttachment cursor;

		PrimitiveHoverable(final Context context) {
			final ObboxStyle.Baked style = new ObboxStyle.Baked();
			style.merge(context.syntax.hoverStyle);
			cursor = new CursorAttachment(context, style);
			context.background.getChildren().add(cursor);
		}

		public void setPosition(final Context context, final TextBrick brick, final int index) {
			System.out.format("cursor %d at [%s]\n", index, brick.getText().substring(0, index));
			cursor.setPosition(context, brick, index);
		}

		@Override
		public void clear(final Context context) {
			context.background.getChildren().remove(cursor);
			hoverable = null;
		}

		@Override
		public void click(final Context context) {
			// TODO
		}
	}

	private class Line {
		public void destroy(final Context context) {
			if (brick != null) {
				brick.remove(context);
			}
		}

		public void setText(final Context context, final String text) {
			this.text = text;
			if (brick != null)
				brick.setText(context, text);
		}

		private class LineBrick extends TextBrick {
			@Override
			public VisualNodePart getVisual() {
				return PrimitiveVisualNode.this;
			}

			@Override
			public Properties getPropertiesForTagsChange(
					final Context context, final TagsChange change
			) {
				final Set<Tag> tags = new HashSet<>(hard ? hardTags : softTags);
				tags.removeAll(change.remove);
				tags.addAll(change.add);
				return properties(context.getStyle(tags));
			}

			@Override
			public Brick createNext(final Context context) {
				if (Line.this.index == lines.size() - 1)
					if (PrimitiveVisualNode.this.parent == null)
						return null;
					else
						return PrimitiveVisualNode.this.parent.createNextBrick(context);
				return lines.get(Line.this.index + 1).createBrick(context);
			}

			@Override
			public void destroy(final Context context) {
				brick = null;
				brickCount -= 1;
				if (brickCount == 0) {
					hardStyle = null;
					firstStyle = null;
					softStyle = null;
					hardAlignment = null;
					firstAlignment = null;
					softAlignment = null;
				}
			}

			@Override
			protected Alignment getAlignment(final Style.Baked style) {
				return index == 0 ? firstAlignment : hard ? hardAlignment : softAlignment;
			}

			@Override
			protected Style.Baked getStyle() {
				return index == 0 ? firstStyle : hard ? hardStyle : softStyle;
			}

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				final Context.Hoverable out = PrimitiveVisualNode.this.hover(context, point);
				if (out != null)
					return out;
				if (brick == null)
					return null;
				if (hoverable == null) {
					hoverable = new PrimitiveHoverable(context);
				}
				System.out.format("brick at c %d, point %d\n", brick.getConverse(context), point.converse);
				hoverable.setPosition(context, this, getUnder(context, point));
				return hoverable;
			}
		}

		boolean hard;
		String text;
		LineBrick brick;
		private int index;

		public void setIndex(final Context context, final int index) {
			if (this.index == 0 && brick != null)
				brick.changed(context);
			this.index = index;
		}

		public Brick createBrick(final Context context) {
			if (brick != null)
				throw new AssertionError("brick exists");
			brickCount += 1;
			if (brickCount == 1)
				getStyles(context);
			brick = new LineBrick();
			brick.setText(context, text);
			return brick;
		}
	}

	List<Line> lines = new ArrayList<>();

	public PrimitiveVisualNode(final Context context, final StringProperty data, final Set<Tag> tags) {
		super(HashTreePSet.from(tags).plus(new PartTag("primitive")));
		dataListener = new ChangeListener<String>() {
			@Override
			public void changed(
					final ObservableValue<? extends String> observable, final String oldValue, final String newValue
			) {
				// TODO change binding so Context is passed in
				// TODO make more efficient, don't recreate all lines
				// TODO actually, the data should be a list of string properties (per hard line) rather than a single
				// and at the top level it should be a list binding
				destroy(context);
				Helper.enumerate(Helper.stream(newValue.split("\n"))).forEach(pair -> {
					final Line line = new Line();
					line.setText(context, pair.second);
					line.setIndex(context, pair.first);
					lines.add(line);
				});
			}
		};
		data.addListener(new WeakChangeListener<>(dataListener));
		dataListener.changed(null, null, data.getValue());
	}

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		return lines.get(0).createBrick(context);
	}

	@Override
	public String debugTreeType() {
		return String.format("prim@%s", Integer.toHexString(hashCode()));
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return Iterables.concat(lines
				.stream()
				.map(line -> line.brick == null ?
						null :
						new Pair<Brick, Brick.Properties>(line.brick,
								line.brick.getPropertiesForTagsChange(context, change)
						))
				.filter(properties -> properties != null)
				.collect(Collectors.toList()));
	}

	private void destroy(final Context context) {
		for (final Line line : lines)
			line.destroy(context);
		lines.clear();
	}

	@Override
	public void destroyBricks(final Context context) {
		destroy(context);
	}
}
