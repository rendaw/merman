package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.Selection;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.CursorAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.TextBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.raw.Obbox;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.bonestruct.wall.bricks.LineBrick;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Pair;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.lang.ref.WeakReference;
import java.text.BreakIterator;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.last;

public class VisualPrimitive extends VisualPart {
	// INVARIANT: Leaf nodes must always create at least one brick
	// TODO index line offsets for faster insert/remove
	// TODO compact/expand
	private final ValuePrimitive.Listener dataListener;
	private final Obbox border = null;
	private final ValuePrimitive data;
	public VisualParent parent;

	public class BrickStyle {
		public Alignment softAlignment;
		public Alignment hardAlignment;
		public Alignment firstAlignment;
		public Style.Baked softStyle;
		public Style.Baked hardStyle;
		public Style.Baked firstStyle;

		BrickStyle(final Context context) {
			update(context);
		}

		public void update(final Context context) {
			final PSet<Tag> tags = HashTreePSet.from(tags(context));
			firstStyle = context.getStyle(tags.plus(new StateTag("first")));
			hardStyle = context.getStyle(tags.plus(new StateTag("hard")));
			softStyle = context.getStyle(tags.plus(new StateTag("soft")));
			firstAlignment = getAlignment(firstStyle.alignment);
			hardAlignment = getAlignment(hardStyle.alignment);
			softAlignment = getAlignment(softStyle.alignment);
		}
	}

	private WeakReference<BrickStyle> brickStyle = new WeakReference<>(null);
	public Set<Tag> softTags = new HashSet<>();
	public Set<Tag> hardTags = new HashSet<>();
	public int brickCount = 0;
	public PrimitiveHoverable hoverable;

	@Override
	public void changeTags(final Context context, final TagsChange tagsChange) {
		super.changeTags(context, tagsChange);
	}

	@Override
	public void tagsChanged(final Context context) {
		final boolean fetched = false;
		final BrickStyle style = brickStyle.get();
		if (style == null)
			return;
		style.update(context);
		for (final Line line : lines) {
			if (line.brick == null)
				continue;
			line.brick.changed(context);
		}
	}

	@Override
	public boolean select(final Context context) {
		selection = createSelection(context, 0, 0);
		context.setSelection(selection);
		return true;
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		return lines.get(0).brick;
	}

	@Override
	public Brick getLastBrick(final Context context) {
		return last(lines).brick;
	}

	@Override
	public Hoverable hover(final Context context, final Vector point) {
		if (parent != null) {
			return parent.hover(context, point);
		}
		return null;
	}

	protected Stream<Action> getActions(final Context context) {
		return Stream.of();
	}

	public PrimitiveSelection selection;

	private class RangeAttachment {
		TextBorderAttachment border;
		CursorAttachment cursor;
		Line beginLine;
		Line endLine;
		int beginOffset;
		int endOffset;
		private final ObboxStyle.Baked style;
		Set<VisualAttachmentAdapter.BoundsListener> listeners = new HashSet<>();

		private RangeAttachment(final ObboxStyle.Baked style) {
			this.style = style;
		}

		private void setOffsets(final Context context, final int beginOffset, final int endOffset) {
			final boolean wasPoint = this.beginOffset == this.endOffset;
			this.beginOffset = Math.max(0, Math.min(data.length(), beginOffset));
			this.endOffset = Math.max(beginOffset, Math.min(data.length(), endOffset));
			if (beginOffset == endOffset) {
				if (context.display != null) {
					if (border != null)
						border.destroy(context);
					if (cursor == null) {
						cursor = new CursorAttachment(context, style);
					}
				}
				final int index = findContaining(beginOffset);
				beginLine = endLine = lines.get(index);
				if (beginLine.brick != null) {
					cursor.setPosition(context, beginLine.brick, beginOffset - beginLine.offset);
					ImmutableSet.copyOf(listeners).forEach(l -> {
						l.firstChanged(context, beginLine.brick);
						l.lastChanged(context, beginLine.brick);
					});
				}
			} else {
				if (wasPoint) {
					beginLine = null;
					endLine = null;
				}
				if (context.display != null) {
					if (cursor != null)
						cursor.destroy(context);
					if (border == null) {
						border = new TextBorderAttachment(context, style);
					}
				}
				final int beginIndex = findContaining(beginOffset);
				if (beginLine == null || beginLine.index != beginIndex) {
					beginLine = lines.get(beginIndex);
					if (beginLine.brick != null) {
						border.setFirst(context, beginLine.brick);
						ImmutableSet.copyOf(listeners).forEach(l -> {
							l.firstChanged(context, beginLine.brick);
						});
					}
				}
				border.setFirstIndex(context, beginIndex - beginLine.offset);
				final int endIndex = findContaining(endOffset);
				if (endLine == null || endLine.index != endIndex) {
					endLine = lines.get(endIndex);
					if (endLine.brick != null) {
						border.setLast(context, endLine.brick);
						ImmutableSet.copyOf(listeners).forEach(l -> {
							l.firstChanged(context, beginLine.brick);
						});
					}
				}
				if (context.display != null)
					border.setLastIndex(context, endIndex - endLine.offset);
			}
		}

		private void setOffsets(final Context context, final int offset) {
			setOffsets(context, offset, offset);
		}

		private void setBeginOffset(final Context context, final int offset) {
			setOffsets(context, offset, endOffset);
		}

		private void setEndOffset(final Context context, final int offset) {
			setOffsets(context, beginOffset, offset);
		}

		public void destroy(final Context context) {
			if (border != null)
				border.destroy(context);
			if (cursor != null)
				cursor.destroy(context);
		}

		public void nudge(final Context context) {
			setOffsets(context, beginOffset, endOffset);
		}

		public void addListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			listeners.add(listener);
			if (beginLine != null && beginLine.brick != null)
				listener.firstChanged(context, beginLine.brick);
			if (endLine != null && endLine.brick != null)
				listener.lastChanged(context, endLine.brick);
		}

		public void removeListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			listeners.remove(listener);
		}
	}

	protected class PrimitiveSelection extends Selection {
		final RangeAttachment range;
		final BreakIterator clusterIterator = BreakIterator.getCharacterInstance();
		private final ValuePrimitive.Listener clusterListener = new ValuePrimitive.Listener() {
			@Override
			public void set(final Context context, final String value) {
				clusterIterator.setText(value);
			}

			@Override
			public void added(final Context context, final int index, final String value) {
				clusterIterator.setText(data.get());
			}

			@Override
			public void removed(final Context context, final int index, final int count) {
				clusterIterator.setText(data.get());
			}
		};

		public PrimitiveSelection(
				final Context context, final int beginOffset, final int endOffset
		) {
			final ObboxStyle.Baked style = new ObboxStyle.Baked();
			style.merge(context.syntax.selectStyle);
			range = new RangeAttachment(style);
			range.setOffsets(context, beginOffset, endOffset);
			clusterIterator.setText(data.get());
			data.addListener(this.clusterListener);
			context.actions.put(this, Stream.concat(Stream.of(new Action() {
				@Override
				public void run(final Context context) {
					if (parent != null) {
						parent.selectUp(context);
					}
				}

				@Override
				public String getName() {
					return "exit";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset < data.length()) {
						range.setOffsets(context, clusterIterator.following(range.beginOffset));
					}
				}

				@Override
				public String getName() {
					return "next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset > 0) {
						range.setOffsets(context, clusterIterator.preceding(range.beginOffset));
					}
				}

				@Override
				public String getName() {
					return "previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final BreakIterator iter = BreakIterator.getWordInstance();
					iter.setText(data.get());
					range.setOffsets(context, iter.following(range.beginOffset));
				}

				@Override
				public String getName() {
					return "next_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final BreakIterator iter = BreakIterator.getWordInstance();
					iter.setText(data.get());
					range.setOffsets(context, iter.preceding(range.beginOffset));
				}

				@Override
				public String getName() {
					return "previous_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					range.setOffsets(context, range.beginLine.offset);
				}

				@Override
				public String getName() {
					return "line_begin";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					range.setOffsets(context, range.beginLine.offset + range.beginLine.text.length());
				}

				@Override
				public String getName() {
					return "line_end";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginLine.index < lines.size()) {
						range.setOffsets(context, lines.get(range.beginLine.index + 1).offset);
					}
				}

				@Override
				public String getName() {
					return "next_line";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginLine.index > 0) {
						range.setOffsets(context, lines.get(range.beginLine.index - 1).offset);
					}
				}

				@Override
				public String getName() {
					return "previous_line";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset == range.endOffset) {
						if (range.beginOffset > 0) {
							final int preceding = clusterIterator.preceding(range.beginOffset);
							context.history.apply(context, data.changeRemove(preceding, range.beginOffset - preceding));
						}
					} else
						context.history.apply(context,
								data.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
						);
				}

				@Override
				public String getName() {
					return "delete_previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset == range.endOffset) {
						if (range.endOffset < data.length()) {
							final int following = clusterIterator.following(range.beginOffset);
							context.history.apply(context,
									data.changeRemove(range.beginOffset, following - range.beginOffset)
							);
						}

					} else
						context.history.apply(context,
								data.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
						);
				}

				@Override
				public String getName() {
					return "delete_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange();
					if (range.beginOffset != range.endOffset)
						context.history.apply(context,
								data.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
						);
					context.history.apply(context, data.changeAdd(range.beginOffset, "\n"));
					context.history.finishChange();
				}

				@Override
				public String getName() {
					return "split";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset == range.endOffset) {
						if (range.beginLine.index + 1 < lines.size()) {
							context.history.finishChange();
							context.history.apply(context,
									data.changeRemove(lines.get(range.beginLine.index + 1).offset - 1, 1)
							);
						}
						context.history.finishChange();
					} else {
						context.history.finishChange();
						for (int index = range.beginLine.index + 1; index <= range.endLine.index; ++index) {
							context.history.apply(context, data.changeRemove(lines.get(index).offset - 1, 1));
						}
						context.history.finishChange();
					}
				}

				@Override
				public String getName() {
					return "join";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final ClipboardContent content = new ClipboardContent();
					content.putString(data.get().substring(range.beginOffset, range.endOffset));
					Clipboard.getSystemClipboard().setContent(content);
				}

				@Override
				public String getName() {
					return "copy";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final ClipboardContent content = new ClipboardContent();
					content.putString(data.get().substring(range.beginOffset, range.endOffset));
					Clipboard.getSystemClipboard().setContent(content);
					context.history.finishChange();
					context.history.apply(context, data.changeRemove(range.beginOffset, range.endOffset));
				}

				@Override
				public String getName() {
					return "cut";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final String value = Clipboard.getSystemClipboard().getString();
					if (value != null) {
						context.history.finishChange();
						if (range.beginOffset != range.endOffset)
							context.history.apply(context,
									data.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
							);
						context.history.apply(context, data.changeAdd(range.beginOffset, value));
					}
				}

				@Override
				public String getName() {
					return "paste";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					reset(context);
				}

				@Override
				public String getName() {
					return "reset_selection";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					range.setEndOffset(context, clusterIterator.following(range.endOffset));
				}

				@Override
				public String getName() {
					return "gather_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					range.setBeginOffset(context, clusterIterator.preceding(range.beginOffset));
				}

				@Override
				public String getName() {
					return "gather_previous";
				}
			}), VisualPrimitive.this.getActions(context)).collect(Collectors.toList()));
		}

		@Override
		public void addBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			range.addListener(context, listener);
		}

		@Override
		public void removeBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			range.removeListener(context, listener);
		}

		@Override
		public void clear(final Context context) {
			range.destroy(context);
			selection = null;
			commit(context);
			data.removeListener(clusterListener);
		}

		@Override
		public void receiveText(final Context context, final String text) {
			if (range.beginOffset != range.endOffset)
				context.history.apply(context,
						data.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
				);
			context.history.apply(context, data.changeAdd(range.beginOffset, text));
		}

		@Override
		public VisualPart getVisual() {
			return VisualPrimitive.this;
		}

		private void reset(final Context context) {
			range.setOffsets(context, range.beginOffset);
		}
	}

	protected void commit(final Context context) {

	}

	public class PrimitiveHoverable extends Hoverable {
		RangeAttachment range;

		PrimitiveHoverable(final Context context) {
			final ObboxStyle.Baked style = new ObboxStyle.Baked();
			style.merge(context.syntax.hoverStyle);
			range = new RangeAttachment(style);
		}

		public void setPosition(final Context context, final int offset) {
			range.setOffsets(context, offset);
		}

		@Override
		public void clear(final Context context) {
			range.destroy(context);
			hoverable = null;
		}

		@Override
		public void click(final Context context) {
			if (selection == null) {
				selection = createSelection(context, range.beginOffset, range.endOffset);
				context.setSelection(selection);
			} else {
				selection.range.setOffsets(context, range.beginOffset, range.endOffset);
			}
		}

		@Override
		public NodeType.NodeTypeVisual node() {
			if (VisualPrimitive.this.parent == null)
				return null;
			return VisualPrimitive.this.parent.getNodeVisual();
		}

		@Override
		public VisualPart part() {
			return VisualPrimitive.this;
		}
	}

	public PrimitiveSelection createSelection(
			final Context context, final int beginOffset, final int endOffset
	) {
		return new PrimitiveSelection(context, beginOffset, endOffset);
	}

	public class Line {
		public int offset;

		private Line(final boolean hard) {
			this.hard = hard;
		}

		public void destroy(final Context context) {
			if (brick != null) {
				brick.destroy(context);
			}
		}

		public void setText(final Context context, final String text) {
			this.text = text;
			if (brick != null)
				brick.setText(context, text);
		}

		public final boolean hard;
		String text;
		public LineBrick brick;
		public int index;

		public void setIndex(final Context context, final int index) {
			if (this.index == 0 && brick != null)
				brick.changed(context);
			this.index = index;
		}

		public Brick createBrick(final Context context) {
			if (brick != null)
				return null;
			BrickStyle style = brickStyle.get();
			if (style == null) {
				style = new BrickStyle(context);
				brickStyle = new WeakReference<>(style);
			}
			brick = new LineBrick(VisualPrimitive.this, this, style);
			brick.setText(context, text);
			if (selection != null && (selection.range.beginLine == Line.this || selection.range.endLine == Line.this))
				selection.range.nudge(context);
			return brick;
		}

		public Hoverable hover(final Context context, final Vector point) {
			if (VisualPrimitive.this.selection == null) {
				final Hoverable out = VisualPrimitive.this.hover(context, point);
				if (out != null)
					return out;
			}
			if (hoverable == null) {
				hoverable = new VisualPrimitive.PrimitiveHoverable(context);
			}
			hoverable.setPosition(context, offset + brick.getUnder(context, point));
			return hoverable;
		}

		public Brick createNextBrick(final Context context) {
			if (index == lines.size() - 1) {
				if (parent == null)
					return null;
				else
					return parent.createNextBrick(context);
			}
			return lines.get(index + 1).createBrick(context);
		}

		public Brick createPreviousBrick(final Context context) {
			if (index == 0)
				if (parent == null)
					return null;
				else
					return parent.createPreviousBrick(context);
			return lines.get(index - 1).createBrick(context);
		}
	}

	public List<Line> lines = new ArrayList<>();

	private int findContaining(final int offset) {
		return lines
				.stream()
				.filter(line -> line.offset + line.text.length() >= offset)
				.map(line -> line.index)
				.findFirst()
				.orElseGet(() -> lines.size());
	}

	public VisualPrimitive(final Context context, final ValuePrimitive data, final Set<Tag> tags) {
		super(HashTreePSet.from(tags).plus(new PartTag("primitive")));
		data.visual = this;
		dataListener = new ValuePrimitive.Listener() {
			@Override
			public void set(final Context context, final String newValue) {
				clear(context);
				final Common.Mutable<Integer> offset = new Common.Mutable<>(0);
				enumerate(Arrays.stream(newValue.split("\n", -1))).forEach(pair -> {
					final Line line = new Line(true);
					line.setText(context, pair.second);
					line.setIndex(context, pair.first);
					line.offset = offset.value;
					lines.add(line);
					offset.value += 1 + pair.second.length();
				});
				if (selection != null) {
					selection.range.setOffsets(context,
							Math.max(0, Math.min(newValue.length(), selection.range.beginOffset))
					);
				}
				final Brick previousBrick = parent == null ? null : parent.getPreviousBrick(context);
				final Brick nextBrick = parent == null ? null : parent.getNextBrick(context);
				if (previousBrick != null && nextBrick != null)
					context.fillFromEndBrick(previousBrick);
			}

			@Override
			public void added(final Context context, final int offset, final String value) {
				final Deque<String> segments = new ArrayDeque<>(Arrays.asList(value.split("\n", -1)));
				if (segments.isEmpty())
					return;
				final int originalIndex = findContaining(offset);
				int index = originalIndex;
				Line line = lines.get(index);

				int movingOffset = offset;

				// Insert text into first line at offset
				final StringBuilder builder = new StringBuilder(line.text);
				String segment = segments.pollFirst();
				builder.insert(movingOffset - line.offset, segment);
				String remainder = null;
				if (!segments.isEmpty()) {
					remainder = builder.substring(movingOffset - line.offset + segment.length());
					builder.delete(movingOffset - line.offset + segment.length(), builder.length());
				}
				line.setText(context, builder.toString());
				movingOffset = line.offset;

				// Add new hard lines for remaining segments
				while (true) {
					index += 1;
					movingOffset += line.text.length();
					segment = segments.pollFirst();
					if (segment == null)
						break;
					line = new Line(true);
					line.setText(context, segment);
					line.setIndex(context, index);
					movingOffset += 1;
					line.offset = movingOffset;
					lines.add(index, line);

					if (index == originalIndex + 1) {
						final Brick previousBrick = index == 0 ?
								(parent == null ? null : parent.getPreviousBrick(context)) :
								lines.get(index - 1).brick;
						final Brick nextBrick = index + 1 >= lines.size() ?
								(parent == null ? null : parent.getNextBrick(context)) :
								lines.get(index + 1).brick;
						if (previousBrick != null && nextBrick != null)
							context.fillFromEndBrick(previousBrick);
					}
				}
				if (remainder != null)
					line.setText(context, line.text + remainder);

				// Renumber/adjust offset of following lines
				for (; index < lines.size(); ++index) {
					line = lines.get(index);
					if (line.hard)
						movingOffset += 1;
					line.index = index;
					line.offset = movingOffset;
					movingOffset += line.text.length();
				}

				if (selection != null) {
					final int newBegin;
					if (selection.range.beginOffset < offset)
						newBegin = selection.range.beginOffset;
					else
						newBegin = selection.range.beginOffset + value.length();
					selection.range.setOffsets(context, newBegin);
				}
			}

			@Override
			public void removed(final Context context, final int offset, final int count) {
				int remaining = count;
				int index = findContaining(offset);
				final Line base = lines.get(index);
				int movingOffset = offset;
				while (remaining > 0) {
					final Line line = lines.get(index);
					line.offset -= count - remaining;
					if (line != base && line.hard && movingOffset == line.offset - 1) {
						remaining -= 1;
						movingOffset += 1;
					}
					final int exciseStart = movingOffset - line.offset;
					final int exciseEnd = Math.min(movingOffset - line.offset + remaining, line.text.length());
					final String newText = line.text.substring(0, exciseStart) + line.text.substring(exciseEnd);
					if (line == base) {
						base.setText(context, newText);
					} else {
						if (!newText.isEmpty())
							base.setText(context, base.text + newText);
						line.destroy(context);
						lines.remove(index);
					}
					movingOffset = line.offset + line.text.length();
					remaining -= exciseEnd - exciseStart;
					index += 1;
				}
				enumerate(lines.stream().skip(base.index + 1)).forEach(pair -> {
					pair.second.index = base.index + 1 + pair.first;
					pair.second.offset -= count;
				});
				if (hoverable != null)
					hoverable.clear(context);
				if (selection != null) {
					final int newBegin;
					final int newEnd;
					if (selection.range.beginOffset < offset)
						newBegin = selection.range.beginOffset;
					else if (selection.range.beginOffset < offset + count)
						newBegin = offset;
					else
						newBegin = selection.range.beginOffset - count;
					if (selection.range.endOffset < offset)
						newEnd = selection.range.endOffset;
					else if (selection.range.endOffset < offset + count)
						newEnd = offset;
					else
						newEnd = selection.range.endOffset - count;
					selection.range.setOffsets(context, newBegin, newEnd);
				}
			}
		};
		data.addListener(dataListener);
		dataListener.set(context, data.get());
		this.data = data;
	}

	@Override
	public void setParent(final VisualParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualParent parent() {
		return parent;
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		return lines.get(0).createBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		return last(lines).createBrick(context);
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

	private void clear(final Context context) {
		for (final Line line : lines)
			line.destroy(context);
		lines.clear();
	}

	@Override
	public void destroy(final Context context) {
		data.removeListener(dataListener);
		data.visual = null;
		clear(context);
	}

}
