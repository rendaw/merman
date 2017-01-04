package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.visual.Brick;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Obbox;
import com.zarbosoft.bonestruct.visual.Style;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.VisualNodeParent;
import com.zarbosoft.bonestruct.visual.nodes.bricks.TextBrick;
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
	private Context.Hoverable hoverable;

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
	public Context.Hoverable hover(final Context context) {
		Context.Hoverable parentHoverable = null;
		if (parent != null && (parentHoverable = parent.hover(context)) != null)
			return parentHoverable;
		if (hoverable == null) {
			hoverable = new Context.Hoverable() {

				// TODO hovery stuff

				@Override
				public void clear(final Context context) {
					hoverable = null;
				}
			};
		}
		return hoverable;
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
