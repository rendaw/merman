package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.ObboxStyle;
import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.CursorAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.TextBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.bricks.TextBrick;
import com.zarbosoft.bonestruct.editor.visual.raw.Obbox;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Mutable;
import com.zarbosoft.pidgoon.internal.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;
import java.util.stream.Collectors;

public class PrimitiveVisualNode extends VisualNodePart {
	// INVARIANT: Leaf nodes must always create at least one brick
	// TODO index line offsets for faster insert/remove
	// TODO compact/expand
	private final DataPrimitive.Listener dataListener;
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
		if (lines.get(0).brick == null)
			return false;
		selection = new PrimitiveSelection(context, lines.get(0).brick, 0);
		context.setSelection(selection);
		return true;
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

	PrimitiveSelection selection;

	private class PrimitiveSelection extends Context.Selection {
		TextBorderAttachment border;
		CursorAttachment cursor;
		int beginIndex;
		TextBrick beginBrick;
		int endIndex;
		TextBrick endBrick;

		public PrimitiveSelection(final Context context, final TextBrick brick, final int index) {
			beginBrick = endBrick = brick;
			beginIndex = endIndex = index;
			final ObboxStyle.Baked style = new ObboxStyle.Baked();
			style.merge(context.syntax.selectStyle);
			cursor = new CursorAttachment(context, style);
			cursor.setPosition(context, beginBrick, beginIndex);
		}

		@Override
		public void clear(final Context context) {
			if (border != null)
				border.destroy(context);
			if (cursor != null)
				cursor.destroy(context);
			selection = null;
			commit(context);
		}

		@Override
		protected Hotkeys getHotkeys(final Context context) {
			return context.getHotkeys(tags());
		}

		@Override
		public Iterable<Context.Action> getActions(final Context context) {
			return PrimitiveVisualNode.this.getActions(context);
		}
	}

	protected void commit(final Context context) {

	}

	private class PrimitiveHoverable extends Context.Hoverable {
		final CursorAttachment cursor;
		TextBrick brick;
		int index;

		PrimitiveHoverable(final Context context) {
			final ObboxStyle.Baked style = new ObboxStyle.Baked();
			style.merge(context.syntax.hoverStyle);
			cursor = new CursorAttachment(context, style);
		}

		public void setPosition(final Context context, final TextBrick brick, final int index) {
			this.brick = brick;
			this.index = index;
			cursor.setPosition(context, brick, index);
		}

		@Override
		public void clear(final Context context) {
			context.background.getChildren().remove(cursor);
			hoverable = null;
		}

		@Override
		public void click(final Context context) {
			selection = new PrimitiveSelection(context, brick, index);
			context.setSelection(selection);
		}
	}

	private class Line {
		public int offset;

		private Line(final boolean hard) {
			this.hard = hard;
		}

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
				hoverable.setPosition(context, this, getUnder(context, point));
				return hoverable;
			}
		}

		final boolean hard;
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

	public PrimitiveVisualNode(final Context context, final DataPrimitive.Value data, final Set<Tag> tags) {
		super(HashTreePSet.from(tags).plus(new PartTag("primitive")));
		dataListener = new DataPrimitive.Listener() {
			@Override
			public void set(final Context context, final String newValue) {
				destroy(context);
				final Mutable<Integer> offset = new Mutable<>(0);
				Helper.enumerate(Helper.stream(newValue.split("\n"))).forEach(pair -> {
					final Line line = new Line(true);
					line.setText(context, pair.second);
					line.setIndex(context, pair.first);
					line.offset = offset.value;
					lines.add(line);
					offset.value += 1 + pair.second.length();
				});
			}

			private int findContaining(final int offset) {
				final Optional<Integer> found = Helper
						.enumerate(lines.stream())
						.filter(pair -> pair.second.offset + pair.second.text.length() >= offset)
						.map(pair -> pair.first)
						.findFirst();
				if (found.isPresent())
					return found.get();
				return lines.size();
			}

			@Override
			public void added(final Context context, int offset, final String value) {
				final Deque<String> segments = new ArrayDeque<>(Arrays.asList(value.split("\n")));
				if (segments.isEmpty())
					return;
				int index = findContaining(offset);
				Line line = lines.get(index);

				// Insert text into first line at offset
				final StringBuilder builder = new StringBuilder(line.text);
				String segment = segments.pollFirst();
				builder.insert(offset - line.offset, segment);
				String remainder = null;
				if (!segments.isEmpty()) {
					remainder = builder.substring(offset - line.offset + segment.length());
					builder.delete(offset - line.offset + segment.length(), remainder.length());
				}
				line.setText(context, builder.toString());
				offset = line.offset;

				// Add new hard lines for remaining segments
				while (true) {
					index += 1;
					offset += line.text.length();
					segment = segments.pollFirst();
					if (segment == null)
						break;
					line = new Line(true);
					line.setText(context, segment);
					line.setIndex(context, index);
					offset += 1;
					line.offset = offset;
					lines.add(index, line);
				}
				if (remainder != null)
					line.setText(context, line.text + remainder);

				// Renumber/adjust offset of following lines
				for (; index < lines.size(); ++index) {
					line = lines.get(index);
					if (line.hard)
						offset += 1;
					line.index = index;
					line.offset = offset;
					offset += line.text.length();
				}
			}

			@Override
			public void removed(final Context context, int offset, final int count) {
				int remaining = count;
				int index = findContaining(offset);
				final Line base = lines.get(index);
				while (remaining > 0) {
					final Line line = lines.get(index);
					line.offset -= count - remaining;
					if (offset == line.offset && line.hard)
						remaining -= 1;
					final String newText = line.text.substring(0, offset - line.offset) +
							line.text.substring(Math.min(remaining, line.text.length()));
					if (line == base) {
						base.setText(context, newText);
					} else {
						if (!newText.isEmpty())
							base.setText(context, base.text + newText);
						line.destroy(context);
						lines.remove(index);
					}
					offset = line.offset + line.text.length();
					remaining -= line.text.length() - newText.length();
					index += 1;
				}
				lines.stream().skip(index).forEach(following -> following.offset -= count);
			}
		};
		data.addListener(dataListener);
		dataListener.set(context, data.get());
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
