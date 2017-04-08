package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.ObboxStyle;
import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.CursorAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.TextBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.bricks.TextBrick;
import com.zarbosoft.bonestruct.editor.visual.raw.Obbox;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Pair;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.text.BreakIterator;
import java.util.*;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.last;

public class PrimitiveVisualNode extends VisualNodePart {
	// INVARIANT: Leaf nodes must always create at least one brick
	// TODO index line offsets for faster insert/remove
	// TODO compact/expand
	private final DataPrimitive.Listener dataListener;
	private final Obbox border = null;
	private final DataPrimitive.Value data;
	private VisualNodeParent parent;
	Alignment softAlignment, hardAlignment, firstAlignment;
	Style.Baked softStyle, hardStyle, firstStyle;
	Set<Tag> softTags = new HashSet<>(), hardTags = new HashSet<>();
	int brickCount = 0;
	private PrimitiveHoverable hoverable;

	private void getStyles(final Context context) {
		final PSet<Tag> tags = HashTreePSet.from(tags());
		firstStyle = context.getStyle(tags.plus(new StateTag("first")));
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
		selection = createSelection(context, 0, 0);
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
		return last(lines).brick;
	}

	@Override
	public Context.Hoverable hover(final Context context, final Vector point) {
		if (parent != null) {
			return parent.hover(context, point);
		}
		return null;
	}

	protected Iterable<Context.Action> getActions(final Context context) {
		return ImmutableList.of();
	}

	PrimitiveSelection selection;

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

	protected class PrimitiveSelection extends Context.Selection {
		final RangeAttachment range;
		final boolean direct;
		final BreakIterator clusterIterator = BreakIterator.getCharacterInstance();
		private final DataPrimitive.Listener clusterListener = new DataPrimitive.Listener() {
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
				final Context context, final int beginOffset, final int endOffset, final boolean direct
		) {
			final ObboxStyle.Baked style = new ObboxStyle.Baked();
			style.merge(context.syntax.selectStyle);
			range = new RangeAttachment(style);
			range.setOffsets(context, beginOffset, endOffset);
			this.direct = direct;
			clusterIterator.setText(data.get());
			data.addListener(this.clusterListener);
		}

		public PrimitiveSelection(final Context context, final int beginOffset, final int endOffset) {
			this(context, beginOffset, endOffset, context.syntax.modalPrimitiveEditing ? false : true);
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
		protected Hotkeys getHotkeys(final Context context) {
			return context.getHotkeys(tags());
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
		public Iterable<Context.Action> getActions(final Context context) {
			final String prefix = direct ? "text-" : "";
			final Iterable<Context.Action> out = Iterables.concat(ImmutableList.of(new Context.Action() {
				@Override
				public void run(final Context context) {
					if (parent != null) {
						parent.selectUp(context);
					}
				}

				@Override
				public String getName() {
					return prefix + "exit";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset < data.length()) {
						range.setOffsets(context, clusterIterator.following(range.beginOffset));
					}
				}

				@Override
				public String getName() {
					return prefix + "next";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					if (range.beginOffset > 0) {
						range.setOffsets(context, clusterIterator.preceding(range.beginOffset));
					}
				}

				@Override
				public String getName() {
					return prefix + "previous";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					final BreakIterator iter = BreakIterator.getWordInstance();
					iter.setText(data.get());
					range.setOffsets(context, iter.following(range.beginOffset));
				}

				@Override
				public String getName() {
					return prefix + "next-word";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					final BreakIterator iter = BreakIterator.getWordInstance();
					iter.setText(data.get());
					range.setOffsets(context, iter.preceding(range.beginOffset));
				}

				@Override
				public String getName() {
					return prefix + "previous-word";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					range.setOffsets(context, range.beginLine.offset);
				}

				@Override
				public String getName() {
					return prefix + "line-begin";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					range.setOffsets(context, range.beginLine.offset + range.beginLine.text.length());
				}

				@Override
				public String getName() {
					return prefix + "line-end";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					if (range.beginLine.index < lines.size()) {
						range.setOffsets(context, lines.get(range.beginLine.index + 1).offset);
					}
				}

				@Override
				public String getName() {
					return prefix + "next-line";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					if (range.beginLine.index > 0) {
						range.setOffsets(context, lines.get(range.beginLine.index - 1).offset);
					}
				}

				@Override
				public String getName() {
					return prefix + "previous-line";
				}
			}, new Context.Action() {
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
					return prefix + "delete-previous";
				}
			}, new Context.Action() {
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
					return prefix + "delete-next";
				}
			}, new Context.Action() {
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
					return prefix + "split";
				}
			}, new Context.Action() {
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
					return prefix + "join";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					final ClipboardContent content = new ClipboardContent();
					content.putString(data.get().substring(range.beginOffset, range.endOffset));
					Clipboard.getSystemClipboard().setContent(content);
				}

				@Override
				public String getName() {
					return prefix + "copy";
				}
			}, new Context.Action() {
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
					return prefix + "cut";
				}
			}, new Context.Action() {
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
					return prefix + "paste";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					reset(context);
				}

				@Override
				public String getName() {
					return prefix + "reset-selection";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					range.setEndOffset(context, clusterIterator.following(range.endOffset));
				}

				@Override
				public String getName() {
					return prefix + "gather-next";
				}
			}, new Context.Action() {
				@Override
				public void run(final Context context) {
					range.setBeginOffset(context, clusterIterator.preceding(range.beginOffset));
				}

				@Override
				public String getName() {
					return prefix + "gather-previous";
				}
			}), PrimitiveVisualNode.this.getActions(context));
			if (context.syntax.modalPrimitiveEditing) {
				if (direct)
					return Iterables.concat(out, ImmutableList.of(new Context.Action() {
						@Override
						public void run(final Context context) {
							context.setSelection(createSelection(context, range.beginOffset, range.endOffset, false));
						}

						@Override
						public String getName() {
							return prefix + "exit";
						}
					}));
				else
					return Iterables.concat(out, ImmutableList.of(new Context.Action() {
						@Override
						public void run(final Context context) {
							context.setSelection(createSelection(context, range.beginOffset, range.endOffset, true));
						}

						@Override
						public String getName() {
							return "enter";
						}
					}));
			} else
				return out;
		}

		@Override
		public VisualNodePart getVisual() {
			return PrimitiveVisualNode.this;
		}

		private void reset(final Context context) {
			range.setOffsets(context, range.beginOffset);
		}
	}

	protected void commit(final Context context) {

	}

	private class PrimitiveHoverable extends Context.Hoverable {
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
			selection = createSelection(context, range.beginOffset, range.endOffset);
			context.setSelection(selection);
		}

		@Override
		public NodeType.NodeTypeVisual node() {
			if (PrimitiveVisualNode.this.parent == null)
				return null;
			return PrimitiveVisualNode.this.parent.getNode();
		}

		@Override
		public VisualNodePart part() {
			return PrimitiveVisualNode.this;
		}
	}

	private PrimitiveSelection createSelection(final Context context, final int beginOffset, final int endOffset) {
		return createSelection(context, beginOffset, endOffset, context.syntax.modalPrimitiveEditing ? false : true);
	}

	public PrimitiveSelection createSelection(
			final Context context, final int beginOffset, final int endOffset, final boolean direct
	) {
		return new PrimitiveSelection(context, beginOffset, endOffset, direct);
	}

	private class Line {
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
				if (Line.this.index == lines.size() - 1) {
					if (PrimitiveVisualNode.this.parent == null)
						return null;
					else
						return PrimitiveVisualNode.this.parent.createNextBrick(context);
				}
				return lines.get(Line.this.index + 1).createBrick(context);
			}

			@Override
			public Brick createPrevious(final Context context) {
				if (Line.this.index == 0)
					if (PrimitiveVisualNode.this.parent == null)
						return null;
					else
						return PrimitiveVisualNode.this.parent.createPreviousBrick(context);
				return lines.get(Line.this.index - 1).createBrick(context);
			}

			@Override
			public void destroyed(final Context context) {
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
				return Line.this.index == 0 ? firstAlignment : hard ? hardAlignment : softAlignment;
			}

			@Override
			protected Style.Baked getStyle() {
				return Line.this.index == 0 ? firstStyle : hard ? hardStyle : softStyle;
			}

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				if (selection == null) {
					final Context.Hoverable out = PrimitiveVisualNode.this.hover(context, point);
					if (out != null)
						return out;
				}
				if (brick == null)
					return null;
				if (hoverable == null) {
					hoverable = new PrimitiveHoverable(context);
				}
				hoverable.setPosition(context, offset + getUnder(context, point));
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
				return null;
			brickCount += 1;
			if (brickCount == 1)
				getStyles(context);
			brick = new LineBrick();
			brick.setText(context, text);
			if (selection != null && (selection.range.beginLine == Line.this || selection.range.endLine == Line.this))
				selection.range.nudge(context);
			return brick;
		}
	}

	List<Line> lines = new ArrayList<>();

	private int findContaining(final int offset) {
		return lines
				.stream()
				.filter(line -> line.offset + line.text.length() >= offset)
				.map(line -> line.index)
				.findFirst()
				.orElseGet(() -> lines.size());
	}

	public PrimitiveVisualNode(final Context context, final DataPrimitive.Value data, final Set<Tag> tags) {
		super(HashTreePSet.from(tags).plus(new PartTag("primitive")));
		data.visual = this;
		dataListener = new DataPrimitive.Listener() {
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
