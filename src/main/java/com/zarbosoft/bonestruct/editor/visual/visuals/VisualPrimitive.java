package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.display.Font;
import com.zarbosoft.bonestruct.editor.display.derived.Obbox;
import com.zarbosoft.bonestruct.editor.visual.*;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.CursorAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.TextBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.editor.wall.bricks.BrickLine;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.text.BreakIterator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.*;

public class VisualPrimitive extends Visual implements VisualLeaf {
	// INVARIANT: Leaf nodes must always create at least one brick
	// TODO index line offsets for faster insert/remove
	private final ValuePrimitive.Listener dataListener;
	private final Obbox border = null;
	private final ValuePrimitive value;
	public VisualParent parent;
	private boolean canExpand = false;
	private boolean canCompact = true;
	private int hardLineCount = 0;

	public class BrickStyle {
		public Style.Baked softStyle;
		public Style.Baked hardStyle;
		public Style.Baked firstStyle;

		BrickStyle(final Context context) {
			update(context);
		}

		public void update(final Context context) {
			firstStyle = context.getStyle(context.globalTags.plusAll(firstTags()));
			hardStyle = context.getStyle(context.globalTags.plusAll(hardTags()));
			softStyle = context.getStyle(context.globalTags.plusAll(softTags()));
		}
	}

	private final BrickStyle brickStyle;
	public PSet<Tag> tags;
	public int brickCount = 0;
	public PrimitiveHoverable hoverable;

	public void tagsChanged(final Context context) {
		final boolean fetched = false;
		brickStyle.update(context);
		for (final Line line : lines) {
			line.styleChanged(context, brickStyle);
		}
		if (selection != null)
			context.selectionTagsChanged();
	}

	@Override
	public void globalTagsChanged(final Context context) {
		tagsChanged(context);
	}

	@Override
	public void changeTags(final Context context, final TagsChange change) {
		tags = change.apply(tags);
		tagsChanged(context);
	}

	@Override
	public boolean canCreateBricks(final Context context) {
		return true;
	}

	public PSet<Tag> softTags() {
		return tags.plus(new StateTag("soft"));
	}

	public PSet<Tag> hardTags() {
		return tags.plus(new StateTag("hard"));
	}

	public PSet<Tag> firstTags() {
		return hardTags().plus(new StateTag("first"));
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

	@Override
	public Stream<Brick> streamBricks() {
		return lines.stream().filter(line -> line.brick != null).map(line -> line.brick);
	}

	protected Stream<Action> getActions(final Context context) {
		return Stream.of();
	}

	public PrimitiveSelection selection;

	public class RangeAttachment {
		boolean leadFirst;
		TextBorderAttachment border;
		CursorAttachment cursor;
		Line beginLine;
		Line endLine;
		public int beginOffset;
		public int endOffset;
		private ObboxStyle.Baked style;
		Set<VisualAttachmentAdapter.BoundsListener> listeners = new HashSet<>();

		private RangeAttachment() {
		}

		private void setOffsetsInternal(final Context context, final int beginOffset, final int endOffset) {
			final boolean wasPoint = this.beginOffset == this.endOffset;
			this.beginOffset = Math.max(0, Math.min(value.length(), beginOffset));
			this.endOffset = Math.max(beginOffset, Math.min(value.length(), endOffset));
			if (beginOffset == endOffset) {
				if (border != null)
					border.destroy(context);
				if (cursor == null) {
					cursor = new CursorAttachment(context);
					cursor.setStyle(context, style);
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
				if (cursor != null)
					cursor.destroy(context);
				if (border == null) {
					border = new TextBorderAttachment(context);
					border.setStyle(context, style);
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
				border.setFirstIndex(context, beginOffset - beginLine.offset);
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
				border.setLastIndex(context, endOffset - endLine.offset);
			}
		}

		private void setOffsets(final Context context, final int beginOffset, final int endOffset) {
			setOffsetsInternal(context, beginOffset, endOffset);
			context.foreground.setCornerstone(context, (leadFirst ? beginLine : endLine).createOrGetBrick(context));
		}

		private void setOffsets(final Context context, final int offset) {
			setOffsetsInternal(context, offset, offset);
			context.foreground.setCornerstone(context, (leadFirst ? beginLine : endLine).createOrGetBrick(context));
		}

		private void setBeginOffset(final Context context, final int offset) {
			setOffsetsInternal(context, offset, endOffset);
			leadFirst = true;
			context.foreground.setCornerstone(context, beginLine.createOrGetBrick(context));
		}

		private void setEndOffset(final Context context, final int offset) {
			setOffsetsInternal(context, beginOffset, offset);
			leadFirst = false;
			context.foreground.setCornerstone(context, endLine.createOrGetBrick(context));
		}

		public void destroy(final Context context) {
			if (border != null)
				border.destroy(context);
			if (cursor != null)
				cursor.destroy(context);
		}

		public void nudge(final Context context) {
			setOffsetsInternal(context, beginOffset, endOffset);
			context.foreground.setCornerstone(context, (leadFirst ? beginLine : endLine).createOrGetBrick(context));
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

		public void setStyle(final Context context, final ObboxStyle.Baked style) {
			this.style = style;
			if (border != null)
				border.setStyle(context, style);
			if (cursor != null)
				cursor.setStyle(context, style);
		}
	}

	public class PrimitiveSelection extends Selection {
		public final RangeAttachment range;
		final BreakIterator clusterIterator = BreakIterator.getCharacterInstance();
		private final ValuePrimitive.Listener clusterListener = new ValuePrimitive.Listener() {
			@Override
			public void set(final Context context, final String text) {
				clusterIterator.setText(text);
			}

			@Override
			public void added(final Context context, final int index, final String text) {
				clusterIterator.setText(value.get());
			}

			@Override
			public void removed(final Context context, final int index, final int count) {
				clusterIterator.setText(value.get());
			}
		};

		@Override
		public PSet<Tag> getTags(final Context context) {
			return tags;
		}

		private int preceding(final BreakIterator iter, final int offset) {
			int to = iter.preceding(offset);
			if (to == BreakIterator.DONE)
				to = 0;
			return Math.max(0, to);
		}

		private int preceding(final BreakIterator iter) {
			return preceding(iter, range.beginOffset);
		}

		private int preceding(final int offset) {
			return preceding(clusterIterator, offset);
		}

		private int preceding() {
			return preceding(clusterIterator, range.beginOffset);
		}

		private int following(final BreakIterator iter, final int offset) {
			int to = iter.following(offset);
			if (to == BreakIterator.DONE)
				to = value.length();
			return Math.min(value.length(), to);
		}

		private int following(final int offset) {
			return following(clusterIterator, offset);
		}

		private int following(final BreakIterator iter) {
			return following(iter, range.endOffset);
		}

		private int following() {
			return following(clusterIterator, range.endOffset);
		}

		private int nextWord(final int source) {
			final BreakIterator iter = BreakIterator.getWordInstance();
			iter.setText(value.get());
			return following(iter, source);
		}

		private int previousWord(final int source) {
			final BreakIterator iter = BreakIterator.getWordInstance();
			iter.setText(value.get());
			return preceding(iter, source);
		}

		private int nextLine(final Line sourceLine, final int source) {
			if (sourceLine.index + 1 < lines.size()) {
				final Line nextLine = lines.get(sourceLine.index + 1);
				return nextLine.offset + Math.min(nextLine.text.length(), source - sourceLine.offset);
			} else
				return sourceLine.offset + sourceLine.text.length();
		}

		private int previousLine(final Line sourceLine, final int source) {
			if (sourceLine.index > 0) {
				final Line previousLine = lines.get(sourceLine.index - 1);
				return previousLine.offset + Math.min(previousLine.text.length(), source - sourceLine.offset);
			} else
				return sourceLine.offset;
		}

		private int endOfLine(final Line sourceLine) {
			return sourceLine.offset + sourceLine.text.length();
		}

		private int startOfLine(final Line sourceLine) {
			return sourceLine.offset;
		}

		public PrimitiveSelection(
				final Context context, final boolean leadFirst, final int beginOffset, final int endOffset
		) {
			range = new RangeAttachment();
			range.setStyle(context, getBorderStyle(context, tags).obbox);
			range.leadFirst = leadFirst;
			range.setOffsets(context, beginOffset, endOffset);
			clusterIterator.setText(value.get());
			value.addListener(this.clusterListener);
			context.addActions(this, Stream.concat(Stream.of(new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					if (value.parent != null) {
						value.parent.selectUp(context);
					}
				}

				@Override
				public String getName() {
					return "exit";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, following());
				}

				@Override
				public String getName() {
					return "next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, preceding());
				}

				@Override
				public String getName() {
					return "previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, nextWord(range.endOffset));
				}

				@Override
				public String getName() {
					return "next_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, previousWord(range.beginOffset));
				}

				@Override
				public String getName() {
					return "previous_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, startOfLine(range.beginLine));
				}

				@Override
				public String getName() {
					return "line_begin";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, endOfLine(range.endLine));
				}

				@Override
				public String getName() {
					return "line_end";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, nextLine(range.endLine, range.endOffset));
				}

				@Override
				public String getName() {
					return "next_line";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setOffsets(context, previousLine(range.beginLine, range.beginOffset));
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
							final int preceding = preceding();
							context.history.apply(context,
									value.changeRemove(preceding, range.beginOffset - preceding)
							);
						}
					} else
						context.history.apply(context,
								value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
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
						if (range.endOffset < value.length()) {
							final int following = following();
							context.history.apply(context,
									value.changeRemove(range.beginOffset, following - range.beginOffset)
							);
						}

					} else
						context.history.apply(context,
								value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
						);
				}

				@Override
				public String getName() {
					return "delete_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					if (range.beginOffset != range.endOffset)
						context.history.apply(context,
								value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
						);
					context.history.apply(context, value.changeAdd(range.beginOffset, "\n"));
					context.history.finishChange(context);
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
							final int select = range.endLine.offset + range.endLine.text.length();
							context.history.finishChange(context);
							context.history.apply(context,
									value.changeRemove(lines.get(range.beginLine.index + 1).offset - 1, 1)
							);
							context.history.finishChange(context);
							select(context, true, select, select);
						}
					} else {
						if (range.beginLine != range.endLine) {
							context.history.finishChange(context);
							final StringBuilder replace = new StringBuilder();
							replace.append(range.beginLine.text.substring(range.beginOffset - range.beginLine.offset));
							final int selectBegin = range.beginOffset;
							int selectEnd = range.endOffset - 1;
							for (int index = range.beginLine.index + 1; index <= range.endLine.index - 1; ++index) {
								replace.append(lines.get(index).text);
								selectEnd -= 1;
							}
							replace.append(range.endLine.text.substring(0, range.endOffset - range.endLine.offset));
							context.history.apply(context,
									value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
							);
							context.history.apply(context, value.changeAdd(range.beginOffset, replace.toString()));
							context.history.finishChange(context);
							select(context, true, selectBegin, selectEnd);
						}
					}
				}

				@Override
				public String getName() {
					return "join";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					context.copy(value.get().substring(range.beginOffset, range.endOffset));
				}

				@Override
				public String getName() {
					return "copy";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					context.copy(value.get().substring(range.beginOffset, range.endOffset));
					context.history.finishChange(context);
					context.history.apply(context,
							value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
					);
					context.history.finishChange(context);
				}

				@Override
				public String getName() {
					return "cut";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					final String text = context.uncopyString();
					if (text != null) {
						context.history.finishChange(context);
						if (range.beginOffset != range.endOffset)
							context.history.apply(context,
									value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
							);
						context.history.apply(context, value.changeAdd(range.beginOffset, text));
					}
					context.history.finishChange(context);
				}

				@Override
				public String getName() {
					return "paste";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, following());
				}

				@Override
				public String getName() {
					return "gather_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, nextWord(range.endOffset));
				}

				@Override
				public String getName() {
					return "gather_next_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, endOfLine(range.endLine));
				}

				@Override
				public String getName() {
					return "gather_next_line_end";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, nextLine(range.endLine, range.endOffset));
				}

				@Override
				public String getName() {
					return "gather_next_line";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, Math.max(range.beginOffset, preceding(range.endOffset)));
				}

				@Override
				public String getName() {
					return "release_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, Math.max(range.beginOffset, previousWord(range.endOffset)));
				}

				@Override
				public String getName() {
					return "release_next_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context, Math.max(range.beginOffset, startOfLine(range.endLine)));
				}

				@Override
				public String getName() {
					return "release_next_line_end";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setEndOffset(context,
							Math.max(range.beginOffset, previousLine(range.endLine, range.endOffset))
					);
				}

				@Override
				public String getName() {
					return "release_next_line";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, preceding());
				}

				@Override
				public String getName() {
					return "gather_previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, previousWord(range.beginOffset));
				}

				@Override
				public String getName() {
					return "gather_previous_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, startOfLine(range.beginLine));
				}

				@Override
				public String getName() {
					return "gather_previous_line_start";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, previousLine(range.beginLine, range.beginOffset));
				}

				@Override
				public String getName() {
					return "gather_previous_line";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, Math.min(range.endOffset, following(range.beginOffset)));
				}

				@Override
				public String getName() {
					return "release_previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, Math.min(range.endOffset, nextWord(range.beginOffset)));
				}

				@Override
				public String getName() {
					return "release_previous_word";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context, Math.min(range.endOffset, endOfLine(range.beginLine)));
				}

				@Override
				public String getName() {
					return "release_previous_line_start";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					range.setBeginOffset(context,
							Math.min(range.endOffset, nextLine(range.beginLine, range.beginOffset))
					);
				}

				@Override
				public String getName() {
					return "release_previous_line";
				}
			}), VisualPrimitive.this.getActions(context)).collect(Collectors.toList()));
		}

		@Override
		public void clear(final Context context) {
			context.removeActions(this);
			range.destroy(context);
			selection = null;
			commit(context);
			value.removeListener(clusterListener);
		}

		@Override
		public void receiveText(final Context context, final String text) {
			if (range.beginOffset != range.endOffset)
				context.history.apply(context,
						value.changeRemove(range.beginOffset, range.endOffset - range.beginOffset)
				);
			context.history.apply(context, value.changeAdd(range.beginOffset, text));
		}

		@Override
		public Visual getVisual() {
			return VisualPrimitive.this;
		}

		@Override
		public SelectionState saveState() {
			return new PrimitiveSelectionState(value, range.leadFirst, range.beginOffset, range.endOffset);
		}

		@Override
		public Path getPath() {
			return value.getPath().add(String.valueOf(range.beginOffset));
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			range.setStyle(context, getBorderStyle(context, tags).obbox);
			super.tagsChanged(context);
		}
	}

	public static class PrimitiveSelectionState implements SelectionState {

		private final ValuePrimitive value;
		private final int beginOffset;
		private final int endOffset;
		private final boolean leadFirst;

		public PrimitiveSelectionState(
				final ValuePrimitive value, final boolean leadFirst, final int beginOffset, final int endOffset
		) {
			this.value = value;
			this.leadFirst = leadFirst;
			this.beginOffset = beginOffset;
			this.endOffset = endOffset;
		}

		@Override
		public void select(final Context context) {
			((VisualPrimitive) value.visual).select(context, leadFirst, beginOffset, endOffset);
		}
	}

	public void select(final Context context, final boolean leadFirst, final int beginOffset, final int endOffset) {
		if (selection != null) {
			selection.range.leadFirst = leadFirst;
			selection.range.setOffsets(context, beginOffset, endOffset);
		} else {
			selection = createSelection(context, leadFirst, beginOffset, endOffset);
			context.setSelection(selection);
		}
	}

	protected void commit(final Context context) {

	}

	public class PrimitiveHoverable extends Hoverable {
		RangeAttachment range;

		PrimitiveHoverable(final Context context) {
			range = new RangeAttachment();
			range.setStyle(context, getBorderStyle(context, tags).obbox);
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
			select(context, true, range.beginOffset, range.endOffset);
		}

		@Override
		public VisualAtom atom() {
			return VisualPrimitive.this.parent.atomVisual();
		}

		@Override
		public Visual visual() {
			return VisualPrimitive.this;
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			range.setStyle(context, getBorderStyle(context, tags).obbox);
		}
	}

	public PrimitiveSelection createSelection(
			final Context context, final boolean leadFirst, final int beginOffset, final int endOffset
	) {
		return new PrimitiveSelection(context, leadFirst, beginOffset, endOffset);
	}

	public class Line implements BrickInterface {
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
		public String text;
		public BrickLine brick;
		public int index;

		public void setIndex(final Context context, final int index) {
			if (this.index == 0 && brick != null)
				brick.changed(context);
			this.index = index;
		}

		public Brick createBrick(final Context context) {
			if (brick != null)
				return null;
			brick = new BrickLine(context, this);
			styleChanged(context, brickStyle);
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
				return parent.createNextBrick(context);
			}
			return lines.get(index + 1).createBrick(context);
		}

		public Brick createPreviousBrick(final Context context) {
			if (index == 0)
				return parent.createPreviousBrick(context);
			return lines.get(index - 1).createBrick(context);
		}

		@Override
		public VisualLeaf getVisual() {
			return VisualPrimitive.this;
		}

		@Override
		public Brick createPrevious(final Context context) {
			return createPreviousBrick(context);
		}

		@Override
		public Brick createNext(final Context context) {
			return createNextBrick(context);
		}

		@Override
		public void brickDestroyed(final Context context) {
			brick = null;
		}

		@Override
		public Alignment getAlignment(final Style.Baked style) {
			return parent.atomVisual().getAlignment(style.alignment);
		}

		@Override
		public PSet<Tag> getTags(final Context context) {
			return index == 0 ? firstTags() : hard ? hardTags() : softTags();
		}

		public void styleChanged(final Context context, final BrickStyle style) {
			if (brick == null)
				return;
			brick.setStyle(context, index == 0 ? style.firstStyle : hard ? style.hardStyle : style.softStyle);
		}

		public Brick createOrGetBrick(final Context context) {
			if (brick != null)
				return brick;
			return createBrick(context);
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

	private void idleLayBricks(final Context context, final int start, final int end) {
		final Function<Integer, Brick> accessor = i -> lines.get(i).brick;
		context.idleLayBricks(parent,
				start,
				end - start,
				lines.size(),
				accessor,
				accessor,
				i -> lines.get(start).createBrick(context)
		);
	}

	public VisualPrimitive(
			final Context context, final VisualParent parent, final ValuePrimitive value, final PSet<Tag> tags
	) {
		this.tags = tags.plus(new PartTag("primitive"));
		this.parent = parent;
		brickStyle = new BrickStyle(context);
		value.visual = this;
		dataListener = new ValuePrimitive.Listener() {
			@Override
			public void set(final Context context, final String text) {
				VisualPrimitive.this.set(context, text);
				idleLayBricks(context, 0, lines.size());
			}

			@Override
			public void added(final Context context, final int offset, final String text) {
				final Deque<String> segments = new ArrayDeque<>(Arrays.asList(text.split("\n", -1)));
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
				final int firstLineCreated = index + 1;
				while (true) {
					index += 1;
					movingOffset += line.text.length();
					segment = segments.pollFirst();
					if (segment == null)
						break;
					line = new Line(true);
					hardLineCount += 1;
					line.setText(context, segment);
					line.setIndex(context, index);
					movingOffset += 1;
					line.offset = movingOffset;
					lines.add(index, line);
				}
				final int lastLineCreated = index + 1;
				if (remainder != null)
					line.setText(context, line.text + remainder);

				// Renumber/adjust offset of following lines
				renumber(context, index, movingOffset);

				if (selection != null) {
					final int newBegin;
					if (selection.range.beginOffset < offset)
						newBegin = selection.range.beginOffset;
					else
						newBegin = selection.range.beginOffset + text.length();
					selection.range.setOffsets(context, newBegin);
				}

				idleLayBricks(context, firstLineCreated, lastLineCreated - firstLineCreated);
			}

			@Override
			public void removed(final Context context, final int offset, final int count) {
				int remaining = count;
				final Line base = lines.get(findContaining(offset));

				// Remove text from first line
				{
					final int exciseStart = offset - base.offset;
					final int exciseEnd = Math.min(exciseStart + remaining, base.text.length());
					final String newText = base.text.substring(0, exciseStart) + base.text.substring(exciseEnd);
					base.setText(context, newText);
					remaining -= exciseEnd - exciseStart;
				}

				// Remove text from subsequent lines
				final int index = base.index + 1;
				int removeLines = 0;
				while (remaining > 0) {
					final Line line = lines.get(index);
					if (line.hard) {
						remaining -= 1;
					}
					final int exciseEnd = Math.min(remaining, line.text.length());
					base.setText(context, base.text + line.text.substring(exciseEnd));
					remaining -= exciseEnd;
					if (line.hard)
						hardLineCount -= 1;
					line.destroy(context);
					removeLines += 1;
				}
				lines.subList(base.index + 1, base.index + 1 + removeLines).clear();
				enumerate(lines.stream().skip(base.index + 1)).forEach(pair -> {
					pair.second.index = base.index + 1 + pair.first;
					pair.second.offset -= count;
				});
				if (hoverable != null) {
					if (hoverable.range.beginOffset >= offset + count) {
						hoverable.range.setOffsets(context, hoverable.range.beginOffset - (offset + count));
					} else if (hoverable.range.beginOffset >= offset || hoverable.range.endOffset >= offset) {
						context.clearHover();
					}
				}
				if (selection != null) {
					int newBegin = selection.range.beginOffset;
					int newEnd = selection.range.endOffset;
					if (newBegin >= offset + count)
						newBegin = newBegin - count;
					else if (newBegin >= offset)
						newBegin = offset;
					if (newEnd >= offset + count)
						newEnd = newEnd - count;
					else if (newEnd >= offset)
						newEnd = offset;
					selection.range.setOffsets(context, newBegin, newEnd);
				}
				System.out.format("removed: hard lines %s, lines %s\n", hardLineCount, lines.size());
				decompacted(context);
			}
		};
		value.addListener(dataListener);
		set(context, value.get());
		this.value = value;
	}

	private void set(final Context context, final String text) {
		clear(context);
		final Common.Mutable<Integer> offset = new Common.Mutable<>(0);
		hardLineCount = 0;
		enumerate(Arrays.stream(text.split("\n", -1))).forEach(pair -> {
			final Line line = new Line(true);
			hardLineCount += 1;
			line.setText(context, pair.second);
			line.setIndex(context, pair.first);
			line.offset = offset.value;
			lines.add(line);
			offset.value += 1 + pair.second.length();
		});
		if (selection != null) {
			selection.range.setOffsets(context, Math.max(0, Math.min(text.length(), selection.range.beginOffset)));
		}
	}

	@Override
	public VisualParent parent() {
		return parent;
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		return lines.get(0).createOrGetBrick(context);
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
	public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
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
		for (final Line line : lines) {
			line.destroy(context);
		}
		lines.clear();
		hardLineCount = 0;
		canExpand = false;
		canCompact = true;
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		// Force expand
		final StringBuilder aggregate = new StringBuilder();
		for (int i = lines.size() - 1; i >= 0; --i) {
			final Line line = lines.get(i);
			aggregate.insert(0, line.text);
			if (line.hard) {
				line.setText(context, aggregate.toString());
				aggregate.setLength(0);
			}
		}
		canExpand = false;
		canCompact = true;
		decompacted(context);
	}

	@Override
	public boolean selectDown(final Context context) {
		value.selectDown(context);
		return true;
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (selection != null)
			context.clearSelection();
		if (hoverable != null)
			context.clearHover();
		value.removeListener(dataListener);
		value.visual = null;
		clear(context);
	}

	@Override
	public boolean canCompact() {
		return canCompact;
	}

	@Override
	public boolean canExpand() {
		return canExpand;
	}

	@Override
	public void compact(final Context context) {
		final RebreakResult result = new RebreakResult();
		boolean rebreak = false;
		for (int i = lines.size() - 1; i >= 0; --i) {
			final Line line = lines.get(i);
			if (line.brick == null)
				continue;
			final int edge = line.brick.converseEdge(context);
			if (!rebreak && edge > context.edge) {
				rebreak = true;
			}
			if (line.hard && rebreak) {
				result.merge(rebreak(context, i));
				rebreak = false;
			}
		}
		canCompact = !result.compactLimit;
		final boolean oldCanExpand = canExpand;
		canExpand = hardLineCount < lines.size();
		if (canExpand && !oldCanExpand) {
			changeTagsCompact(context);
		}
	}

	private void decompacted(final Context context) {
		canExpand = hardLineCount < lines.size();
		if (!canExpand) {
			changeTagsExpand(context);
			canCompact = true;
		}
	}

	@Override
	public void expand(final Context context) {
		boolean expanded = false;
		boolean rebreak = true;
		for (int i = lines.size() - 1; i >= 0; --i) {
			final Line line = lines.get(i);
			if (line.brick == null)
				continue;
			if (!rebreak && line.brick.converseEdge(context) * 1.25 > context.edge) {
				rebreak = false;
			}
			if (line.hard && rebreak) {
				expanded = expanded || rebreak(context, i).changed;
				rebreak = true;
			}
		}
		if (expanded)
			canCompact = true;
		decompacted(context);
	}

	private static class RebreakResult {
		boolean changed = false;
		boolean compactLimit = false;

		public void merge(final RebreakResult other) {
			changed = changed || other.changed;
			compactLimit = compactLimit || other.compactLimit;
		}
	}

	private RebreakResult rebreak(final Context context, final int i) {
		final VisualAtom atom = parent.atomVisual();
		final RebreakResult result = new RebreakResult();
		class Builder {
			String text;
			int offset;

			public boolean hasText() {
				return !text.isEmpty();
			}

			public RebreakResult build(final Line line, final Font font, final int converse) {
				final RebreakResult result = new RebreakResult();
				final int width = font.getWidth(text);
				final int edge = converse + width;
				int split;
				if (converse < context.edge && edge > context.edge) {
					final BreakIterator lineIter = BreakIterator.getLineInstance();
					lineIter.setText(text);
					final int edgeOffset = context.edge - converse;
					final int under = font.getUnder(text, edgeOffset);
					if (under == text.length())
						split = under;
					else {
						split = lineIter.preceding(under + 1);
						if (split == 0 || split == BreakIterator.DONE) {
							final BreakIterator clusterIter = BreakIterator.getCharacterInstance();
							clusterIter.setText(text);
							split = clusterIter.preceding(under + 1);
						}
						if (split < 4 || split == BreakIterator.DONE) {
							split = text.length();
							result.compactLimit = true;
						}
					}
				} else {
					split = text.length();
				}

				line.setText(context, text.substring(0, split));
				if (line.offset == offset)
					result.changed = false;
				else
					result.changed = true;
				line.offset = offset;
				text = text.substring(split);
				offset += split;
				return result;
			}
		}
		final Builder build = new Builder();
		final int modifiedOffsetStart = lines.get(i).offset;
		build.offset = modifiedOffsetStart;

		int endIndex = i;
		final int modifiedLength;

		// Get the full unwrapped text
		{
			final StringBuilder sum = new StringBuilder();
			for (int j = i; j < lines.size(); ++j, ++endIndex) {
				final Line line = lines.get(j);
				if (j > i && line.hard)
					break;
				sum.append(line.text);
			}
			build.text = sum.toString();
			modifiedLength = build.text.length();
		}

		int j = i;

		// Wrap text into existing lines
		for (; j < endIndex; ++j) {
			final Line line = lines.get(j);
			if (!build.hasText())
				break;
			final Font font;
			final int converse;
			if (line.brick == null) {
				final Style.Baked style =
						j == 0 ? brickStyle.firstStyle : j == i ? brickStyle.hardStyle : brickStyle.softStyle;
				font = style.getFont(context);
				final Alignment alignment = atom.getAlignment(style.alignment);
				if (alignment == null)
					converse = 0;
				else
					converse = alignment.converse;
			} else {
				font = line.brick.getFont();
				converse = line.brick.getConverse(context);
			}
			result.merge(build.build(line, font, converse));
		}

		// If text remains, make new lines
		Integer firstLineCreated = null;
		Integer lastLineCreated = null;
		if (build.hasText()) {
			final Brick priorBrick = lines.get(j - 1).brick;

			firstLineCreated = j;
			while (build.hasText()) {
				final Line line = new Line(false);
				line.setIndex(context, j);
				final Style.Baked style = brickStyle.softStyle;
				final Font font = style.getFont(context);
				final Alignment alignment = atom.getAlignment(style.alignment);
				final int converse;
				if (alignment == null)
					converse = 0;
				else
					converse = alignment.converse;
				build.build(line, font, converse);
				lines.add(j, line);
				++j;
			}
			lastLineCreated = j;
		}

		// If ran out of text early, delete following soft lines
		if (j < endIndex) {
			result.changed = true;
			for (final Line line : iterable(lines.stream().skip(j))) {
				if (line.hard)
					hardLineCount -= 1;
				line.destroy(context);
			}
			lines.subList(j, endIndex).clear();
		}

		// Cleanup
		renumber(context, j, build.offset);

		if (firstLineCreated != null) {
			idleLayBricks(context, firstLineCreated, lastLineCreated - firstLineCreated);
		}

		// Adjust hover/selection
		if (hoverable != null) {
			if (hoverable.range.beginOffset >= modifiedOffsetStart + modifiedLength) {
				hoverable.range.nudge(context);
			} else if (hoverable.range.beginOffset >= modifiedOffsetStart ||
					hoverable.range.endOffset >= modifiedOffsetStart) {
				context.clearHover();
			}
		}
		if (selection != null) {
			selection.range.nudge(context);
		}

		return result;
	}

	private void renumber(final Context context, int index, int offset) {
		for (; index < lines.size(); ++index) {
			final Line line = lines.get(index);
			if (line.hard)
				offset += 1;
			line.index = index;
			line.offset = offset;
			offset += line.text.length();
		}
	}
}
