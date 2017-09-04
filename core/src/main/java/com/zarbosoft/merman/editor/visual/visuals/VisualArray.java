package com.zarbosoft.merman.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.*;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.visual.*;
import com.zarbosoft.merman.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.editor.wall.bricks.BrickSpace;
import com.zarbosoft.merman.syntax.front.FrontSymbol;
import com.zarbosoft.merman.syntax.middle.MiddleArray;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.last;

public abstract class VisualArray extends VisualGroup implements VisualLeaf {
	private PSet<Tag> tags;
	private PSet<Tag> ellipsisTags;
	private PSet<Tag> emptyTags;
	private final ValueArray.Listener dataListener;
	public final ValueArray value;
	private Brick ellipsis = null;
	private Brick empty = null;

	public VisualArray(
			final Context context,
			final VisualParent parent,
			final ValueArray value,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		super(visualDepth);
		this.tags = tags;
		ellipsisTags = this.tags.plus(new PartTag("ellipsis"));
		emptyTags = this.tags.plus(new PartTag("empty"));
		this.value = value;
		dataListener = new ValueArray.Listener() {

			@Override
			public void changed(final Context context, final int index, final int remove, final List<Atom> add) {
				if (ellipsize(context)) {
					if (value.data.isEmpty()) {
						// Was blank, now ellipsized
						if (empty != null)
							empty.destroy(context);
						context.idleLayBricks(parent, 0, 1, 1, null, null, i -> createEmpty(context));
						return;
					} else if (add.isEmpty() && remove == value.data.size()) {
						// Was ellipsized, now blank
					} else {
						// Was ellipsized, no change
						return;
					}
				}

				// Prep to fix selection if deep under an element
				Integer fixDeepSelectionIndex = null;
				Integer fixDeepHoverIndex = null;
				Integer oldSelectionBeginIndex = null;
				Integer oldSelectionEndIndex = null;
				if (selection != null) {
					oldSelectionBeginIndex = selection.beginIndex;
					oldSelectionEndIndex = selection.endIndex;
				} else if (context.selection != null) {
					VisualParent parent = context.selection.getVisual().parent();
					while (parent != null) {
						final Visual visual = parent.visual();
						if (visual == VisualArray.this) {
							fixDeepSelectionIndex = ((ArrayVisualParent) parent).valueIndex();
							break;
						}
						parent = visual.parent();
					}
				}
				if (hoverable == null && context.hover != null) {
					VisualParent parent = context.hover.visual().parent();
					while (parent != null) {
						final Visual visual = parent.visual();
						if (visual == VisualArray.this) {
							fixDeepHoverIndex = ((ArrayVisualParent) parent).valueIndex();
							break;
						}
						parent = visual.parent();
					}
				}

				// Create child visuals
				coreChange(context, index, remove, add);

				// Lay bricks if children added/totally cleared
				if (!add.isEmpty()) {
					if (empty != null)
						empty.destroy(context);
					final int layIndex = visualIndex(index);
					context.idleLayBricks(parent,
							layIndex,
							visualIndex(index + add.size()) - layIndex,
							children.size(),
							i -> children.get(i).getFirstBrick(context),
							i -> children.get(i).getLastBrick(context),
							i -> children.get(i).createFirstBrick(context)
					);
				} else if (value.data.isEmpty()) {
					if (ellipsis != null)
						ellipsis.destroy(context);
					context.idleLayBricks(parent, 0, 1, 1, null, null, i -> createEmpty(context));
				}

				// Fix hover/selection
				if (hoverable != null) {
					hoverable.notifyRangeAdjusted(context, index, remove, add.size());
				} else if (fixDeepHoverIndex != null &&
						fixDeepHoverIndex >= index &&
						fixDeepHoverIndex < index + remove) {
					context.clearHover();
				}
				if (oldSelectionBeginIndex != null) {
					if (value.data.isEmpty())
						value.parent.selectUp(context);
					else {
						if (oldSelectionBeginIndex >= index + remove)
							selection.setBegin(context, oldSelectionBeginIndex - remove + add.size());
						else if (oldSelectionBeginIndex >= index)
							selection.setBegin(context,
									Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1))
							);
						if (oldSelectionEndIndex >= index + remove)
							selection.setEnd(context, oldSelectionEndIndex - remove + add.size());
						else if (oldSelectionEndIndex >= index)
							selection.setEnd(context,
									Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1))
							);
					}
				} else if (fixDeepSelectionIndex != null) {
					if (value.data.isEmpty())
						value.parent.selectUp(context);
					else if (fixDeepSelectionIndex >= index && fixDeepSelectionIndex < index + remove) {
						final int newIndex = Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1));
						select(context, true, newIndex, newIndex);
					}
				}
			}
		};
		value.addListener(dataListener);
		value.visual = this;
		root(context, parent, alignments, depthScore, depthScore);
	}

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "array";
		}
	}

	private void coreChange(final Context context, final int index, final int remove, final List<Atom> add) {
		final Map<String, Alignment> alignments = parent.atomVisual().alignments();
		int visualIndex = index;
		int visualRemove = remove;
		int visualAdd = add.size();
		if (!getSeparator().isEmpty()) {
			visualIndex = index == 0 ? 0 : visualIndex * 2 - 1;
			visualAdd = children.isEmpty() ? visualAdd * 2 - 1 : visualAdd * 2;
			visualRemove = Math.min(visualRemove * 2, children.size());
		}
		final boolean retagFirst = tagFirst() && visualIndex == 0;
		final boolean retagLast = tagLast() && visualIndex + visualRemove == children.size();
		if (!children.isEmpty() && !add.isEmpty()) {
			if (retagFirst)
				(children.get(0)).changeTags(context, new TagsChange().remove(new PartTag("first")));
			if (retagLast)
				(last(children)).changeTags(context, new TagsChange().remove(new PartTag("last")));
		}

		// Remove
		remove(context, visualIndex, visualRemove);

		// Add
		if (!add.isEmpty()) {
			int addIndex = visualIndex;
			final Consumer<Integer> addSeparator = addAt -> {
				final VisualGroup group =
						new VisualGroup(context, new ArrayVisualParent(addAt, false), visualDepth + 1, depthScore());
				for (int fixIndex = 0; fixIndex < getSeparator().size(); ++fixIndex) {
					final FrontSymbol fix = getSeparator().get(fixIndex);
					group.add(context, fix.createVisual(context,
							group.createParent(fixIndex),
							tags,
							alignments,
							group.visualDepth + 1,
							depthScore()
					));
				}
				if (atomVisual().compact)
					group.compact(context);
				super.add(context, group, addAt);
			};
			for (final Atom atom : add) {
				if (!getSeparator().isEmpty() && addIndex > 0)
					addSeparator.accept(addIndex++);
				final VisualGroup group =
						new VisualGroup(context, new ArrayVisualParent(addIndex, true), visualDepth + 1, depthScore());
				int groupIndex = 0;
				for (final FrontSymbol fix : getPrefix())
					group.add(context, fix.createVisual(context,
							group.createParent(groupIndex++),
							tags,
							alignments,
							group.visualDepth + 1,
							depthScore()
					));
				final VisualAtom nodeVisual = (VisualAtom) atom.createVisual(context,
						group.createParent(groupIndex++),
						alignments,
						group.visualDepth + 2,
						depthScore()
				);
				final int addIndex2 = addIndex;
				group.add(context, new Visual(group.visualDepth + 1) {
					@Override
					public VisualParent parent() {
						return nodeVisual.parent();
					}

					@Override
					public void globalTagsChanged(final Context context) {
						nodeVisual.globalTagsChanged(context);
					}

					@Override
					public void changeTags(final Context context, final TagsChange change) {
					}

					@Override
					public Brick createOrGetFirstBrick(final Context context) {
						return nodeVisual.createOrGetFirstBrick(context);
					}

					@Override
					public Brick createFirstBrick(final Context context) {
						return nodeVisual.createFirstBrick(context);
					}

					@Override
					public Brick createLastBrick(final Context context) {
						return nodeVisual.createLastBrick(context);
					}

					@Override
					public Brick getFirstBrick(final Context context) {
						return nodeVisual.getFirstBrick(context);
					}

					@Override
					public Brick getLastBrick(final Context context) {
						return nodeVisual.getLastBrick(context);
					}

					@Override
					public void compact(final Context context) {
					}

					@Override
					public void expand(final Context context) {

					}

					@Override
					public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
							final Context context, final TagsChange change
					) {
						return ImmutableList.of();
					}

					@Override
					public void uproot(final Context context, final Visual root) {
						nodeVisual.uproot(context, root);
					}

					@Override
					public void root(
							final Context context,
							final VisualParent parent,
							final Map<String, Alignment> alignments,
							final int depth,
							final int depthScore
					) {
						super.root(context, parent, alignments, depth, depthScore);
						nodeVisual.root(context, parent, alignments, depth + 1, depthScore);
					}

					@Override
					public boolean selectDown(final Context context) {
						return nodeVisual.selectDown(context);
					}

					@Override
					public Stream<Brick> streamBricks() {
						return nodeVisual.streamBricks();
					}
				});
				for (final FrontSymbol fix : getSuffix())
					group.add(context, fix.createVisual(context,
							group.createParent(groupIndex++),
							tags,
							alignments,
							group.visualDepth + 1,
							depthScore()
					));
				if (atomVisual().compact)
					group.compact(context);
				super.add(context, group, addIndex++);
			}
			if (!getSeparator().isEmpty() && visualIndex == 0 && value.data.size() > add.size())
				addSeparator.accept(addIndex++);
		}

		// Cleanup
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
			if (retagLast)
				last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
		}
	}

	protected abstract boolean tagLast();

	protected abstract boolean tagFirst();

	protected abstract Symbol ellipsis();

	@Override
	public Stream<Brick> streamBricks() {
		if (empty != null)
			return Stream.of(empty);
		if (ellipsis != null)
			return Stream.of(ellipsis);
		return super.streamBricks();
	}

	protected abstract List<FrontSymbol> getPrefix();

	protected abstract List<FrontSymbol> getSeparator();

	protected abstract List<FrontSymbol> getSuffix();

	private Brick createEllipsis(final Context context) {
		if (ellipsis != null)
			return null;
		ellipsis = ellipsis().createBrick(context, new BrickInterface() {
			@Override
			public VisualLeaf getVisual() {
				return VisualArray.this;
			}

			@Override
			public Brick createPrevious(final Context context) {
				return parent.createPreviousBrick(context);
			}

			@Override
			public Brick createNext(final Context context) {
				return parent.createNextBrick(context);
			}

			@Override
			public void brickDestroyed(final Context context) {
				ellipsis = null;
			}

			@Override
			public Alignment getAlignment(final Style.Baked style) {
				return parent.atomVisual().getAlignment(style.alignment);
			}

			@Override
			public PSet<Tag> getTags(final Context context) {
				return context.globalTags.plusAll(ellipsisTags);
			}
		});
		context.bricksCreated(this, ellipsis);
		return ellipsis;
	}

	private Brick createEmpty(final Context context) {
		if (empty != null)
			return null;
		empty = new BrickSpace(context, new BrickInterface() {
			@Override
			public VisualLeaf getVisual() {
				return VisualArray.this;
			}

			@Override
			public Brick createPrevious(final Context context) {
				return parent.createPreviousBrick(context);
			}

			@Override
			public Brick createNext(final Context context) {
				return parent.createNextBrick(context);
			}

			@Override
			public void brickDestroyed(final Context context) {
				empty = null;
			}

			@Override
			public Alignment getAlignment(final Style.Baked style) {
				return parent.atomVisual().getAlignment(style.alignment);
			}

			@Override
			public PSet<Tag> getTags(final Context context) {
				return context.globalTags.plusAll(emptyTags);
			}
		});
		context.bricksCreated(this, empty);
		return empty;
	}

	private boolean ellipsize(final Context context) {
		if (!context.window)
			return false;
		return parent.atomVisual().depthScore >= context.syntax.ellipsizeThreshold;
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		if (value.data.isEmpty()) {
			if (empty != null)
				return empty;
			else
				return createEmpty(context);
		} else if (ellipsize(context)) {
			if (ellipsis != null)
				return ellipsis;
			else
				return createEllipsis(context);
		} else
			return super.createOrGetFirstBrick(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (value.data.isEmpty())
			return createEmpty(context);
		if (ellipsize(context))
			return createEllipsis(context);
		return super.createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		if (value.data.isEmpty())
			return createEmpty(context);
		if (ellipsize(context))
			return createEllipsis(context);
		return super.createLastBrick(context);
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		if (empty != null)
			return empty;
		if (ellipsize(context))
			return ellipsis;
		return super.getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		if (empty != null)
			return empty;
		if (ellipsize(context))
			return ellipsis;
		return super.getLastBrick(context);
	}

	@Override
	public void root(
			final Context context,
			final VisualParent parent,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		this.parent = parent;
		if (value.data.isEmpty()) {
			super.root(context, parent, alignments, visualDepth, depthScore);
			if (empty == null)
				context.idleLayBricks(parent, 0, 1, 1, null, null, i -> createEmpty(context));
		} else if (ellipsize(context)) {
			if (!children.isEmpty()) {
				remove(context, 0, children.size());
			}
			super.root(context, parent, alignments, visualDepth, depthScore);
			if (ellipsis == null)
				context.idleLayBricks(parent, 0, 1, 1, null, null, i -> createEllipsis(context));
		} else {
			if (ellipsis != null)
				ellipsis.destroy(context);
			super.root(context, parent, alignments, visualDepth, depthScore);
			if (children.isEmpty()) {
				coreChange(context, 0, 0, value.data);
				context.idleLayBricks(parent, 0, 1, 1, null, null, i -> children.get(0).createFirstBrick(context));
			}
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (root == this) {
			// Only root array, which should never be uprooted with itself as the stop point
			throw new AssertionError();
		}
		if (selection != null)
			context.clearSelection();
		if (hoverable != null)
			context.clearHover();
		if (ellipsis != null)
			ellipsis.destroy(context);
		if (empty != null)
			empty.destroy(context);
		value.removeListener(dataListener);
		value.visual = null;
		super.uproot(context, root);
		children.clear();
	}

	public void tagsChanged(final Context context) {
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
		if (empty != null)
			empty.tagsChanged(context);
		if (selection != null)
			selection.tagsChanged(context);
		if (hoverable != null)
			hoverable.tagsChanged(context);
	}

	@Override
	public void globalTagsChanged(
			final Context context
	) {
		tagsChanged(context);
		super.globalTagsChanged(context);
	}

	@Override
	public void changeTags(final Context context, final TagsChange change) {
		tags = change.apply(tags);
		ellipsisTags = this.tags.plus(new PartTag("ellipsis"));
		emptyTags = this.tags.plus(new PartTag("empty"));
		tagsChanged(context);
		super.changeTags(context, change);
	}

	private ArrayHoverable hoverable;

	private abstract class ArrayHoverable extends Hoverable {
		final BorderAttachment border;

		ArrayHoverable(final Context context) {
			border = new BorderAttachment(context, getBorderStyle(context, tags).obbox);
		}

		@Override
		protected void clear(final Context context) {
			border.destroy(context);
			if (hoverable == this)
				hoverable = null;
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			border.setStyle(context, getBorderStyle(context, tags).obbox);
		}

		@Override
		public Visual visual() {
			return VisualArray.this;
		}

		public abstract void notifyRangeAdjusted(Context context, int index, int removed, int added);

		public abstract void notifySelected(Context context, int start, int end);
	}

	private class ElementHoverable extends ArrayHoverable {
		private int index;

		ElementHoverable(final Context context) {
			super(context);
		}

		public void setIndex(final Context context, final int index) {
			this.index = index;
			border.setFirst(context, children.get(visualIndex(index)).getFirstBrick(context));
			border.setLast(context, children.get(visualIndex(index)).getLastBrick(context));
		}

		@Override
		public void click(final Context context) {
			select(context, true, index, index);
		}

		@Override
		public VisualAtom atom() {
			return VisualArray.this.parent.atomVisual();
		}

		@Override
		public void notifyRangeAdjusted(final Context context, final int index, final int removed, final int added) {
			if (this.index >= index + removed) {
				setIndex(context, this.index - removed + added);
			} else if (this.index >= index) {
				context.clearHover();
			}
		}

		@Override
		public void notifySelected(final Context context, final int start, final int end) {
			if (this.index >= start && this.index <= end) {
				context.clearHover();
			}
		}
	}

	private class PlaceholderHoverable extends ArrayHoverable {

		private PlaceholderHoverable(final Context context, final Brick brick) {
			super(context);
			border.setFirst(context, brick);
			border.setLast(context, brick);
		}

		@Override
		public void click(final Context context) {
			select(context, true, 0, 0);
		}

		@Override
		public VisualAtom atom() {
			return VisualArray.this.parent.atomVisual();
		}

		@Override
		public void tagsChanged(final Context context) {
			border.setStyle(context, getBorderStyle(context, tags).obbox);
		}

		@Override
		public void notifyRangeAdjusted(final Context context, final int index, final int removed, final int added) {
			throw new DeadCode();
		}

		@Override
		public void notifySelected(final Context context, final int start, final int end) {
			context.clearHover();
		}
	}

	private int visualIndex(final int valueIndex) {
		if (getSeparator().isEmpty())
			return valueIndex;
		else
			return valueIndex * 2;
	}

	public ArraySelection selection;

	public static class ArraySelection extends Selection {
		public final VisualArray self;
		BorderAttachment border;
		public int beginIndex;
		public int endIndex;
		public boolean leadFirst;

		public ArraySelection(
				final Context context, final VisualArray self, final boolean leadFirst, final int start, final int end
		) {
			this.self = self;
			border = new BorderAttachment(context, getBorderStyle(context, self.tags).obbox);
			this.leadFirst = leadFirst;
			setRange(context, start, end);
			context.addActions(this, ImmutableList.of(new ActionEnter(),
					new ActionExit(),
					new ActionNext(),
					new ActionPrevious(),
					new ActionNextElement(),
					new ActionPreviousElement(),
					new ActionDelete(),
					new ActionInsertBefore(),
					new ActionInsertAfter(),
					new ActionCopy(),
					new ActionCut(),
					new ActionPaste(),
					new ActionGatherNext(),
					new ActionReleaseNext(),
					new ActionGatherPrevious(),
					new ActionReleasePrevious(),
					new ActionMoveBefore(),
					new ActionMoveAfter(),
					new ActionWindow(),
					new ActionPrefix(leadFirst),
					new ActionSuffix(leadFirst)
			));
		}

		private void setCornerstone(final Context context, final int index) {
			context.foreground.setCornerstone(context,
					self.children.get(self.visualIndex(index)).createOrGetFirstBrick(context),
					() -> {
						for (int at = self.visualIndex(index) - 1; at >= 0; --at) {
							final Brick found = self.children.get(at).getLastBrick(context);
							if (found != null)
								return found;
						}
						return self.parent.getPreviousBrick(context);
					},
					() -> {
						for (int at = self.visualIndex(index) + 1; at < self.children.size(); ++at) {
							final Brick found = self.children.get(at).getFirstBrick(context);
							if (found != null)
								return found;
						}
						return self.parent.getNextBrick(context);
					}
			);
		}

		private void setBeginInternal(final Context context, final int index) {
			beginIndex = index;
			if (leadFirst)
				setCornerstone(context, beginIndex);
		}

		private void setEndInternal(final Context context, final int index) {
			endIndex = index;
			if (!leadFirst)
				setCornerstone(context, endIndex);
		}

		private void setBegin(final Context context, final int index) {
			leadFirst = true;
			setBeginInternal(context, index);
			border.setFirst(context, self.children.get(self.visualIndex(index)).getFirstBrick(context));
		}

		private void setEnd(final Context context, final int index) {
			leadFirst = false;
			setEndInternal(context, index);
			border.setLast(context, self.children.get(self.visualIndex(index)).getLastBrick(context));
		}

		private void setRange(final Context context, final int begin, final int end) {
			setBeginInternal(context, begin);
			setEndInternal(context, end);
			border.setFirst(context, self.children.get(self.visualIndex(begin)).getFirstBrick(context));
			border.setLast(context, self.children.get(self.visualIndex(end)).getLastBrick(context));
		}

		private void setPosition(final Context context, final int index) {
			setEndInternal(context, index);
			setBeginInternal(context, index);
			border.setFirst(context, self.children.get(self.visualIndex(index)).getFirstBrick(context));
			border.setLast(context, self.children.get(self.visualIndex(index)).getLastBrick(context));
		}

		@Override
		public void clear(final Context context) {
			border.destroy(context);
			self.selection = null;
			context.removeActions(this);
		}

		@Override
		public Visual getVisual() {
			return self.children.get(beginIndex);
		}

		@Override
		public SelectionState saveState() {
			return new ArraySelectionState(self.value, leadFirst, beginIndex, endIndex);
		}

		@Override
		public Path getPath() {
			return self.value.getPath().add(String.valueOf(beginIndex));
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			border.setStyle(context, getBorderStyle(context, self.tags).obbox);
			super.tagsChanged(context);
		}

		@Override
		public PSet<Tag> getTags(final Context context) {
			return self.tags;
		}

		@Action.StaticID(id = "enter")
		private class ActionEnter extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				return self.value.data.get(beginIndex).visual.selectDown(context);
			}
		}

		@Action.StaticID(id = "exit")
		private class ActionExit extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				return self.value.parent.selectUp(context);
			}
		}

		@Action.StaticID(id = "next")
		private class ActionNext extends ActionBase {
			@Override
			public boolean run(final Context context) {
				return self.parent.selectNext(context);
			}

		}

		@Action.StaticID(id = "previous")
		private class ActionPrevious extends ActionBase {
			@Override
			public boolean run(final Context context) {
				return self.parent.selectPrevious(context);
			}

		}

		@Action.StaticID(id = "next_element")
		private class ActionNextElement extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				ArraySelection.this.leadFirst = true;
				final int newIndex = Math.min(self.value.data.size() - 1, endIndex + 1);
				if (newIndex == beginIndex && newIndex == endIndex)
					return false;
				setPosition(context, newIndex);
				return true;
			}
		}

		@Action.StaticID(id = "previous_element")
		private class ActionPreviousElement extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				ArraySelection.this.leadFirst = true;
				final int newIndex = Math.max(0, beginIndex - 1);
				if (newIndex == beginIndex && newIndex == endIndex)
					return false;
				setPosition(context, newIndex);
				return true;
			}
		}

		@Action.StaticID(id = "delete")
		private class ActionDelete extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.apply(context,
						new ChangeArray(self.value, beginIndex, endIndex - beginIndex + 1, ImmutableList.of())
				);
				return true;
			}
		}

		@Action.StaticID(id = "insert_before")
		private class ActionInsertBefore extends ActionBase {
			@Override
			public boolean run(final Context context) {
				final Atom created = self.value.createAndAddDefault(context, beginIndex);
				if (!created.visual.selectDown(context))
					setPosition(context, beginIndex);
				return true;
			}
		}

		@Action.StaticID(id = "insert_after")
		private class ActionInsertAfter extends ActionBase {
			@Override
			public boolean run(final Context context) {
				final Atom created = self.value.createAndAddDefault(context, endIndex + 1);
				if (!created.visual.selectDown(context))
					setPosition(context, endIndex + 1);
				return true;
			}
		}

		@Action.StaticID(id = "copy")
		private class ActionCopy extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.copy(self.value.data.subList(beginIndex, endIndex + 1));
				return true;
			}

		}

		@Action.StaticID(id = "cut")
		private class ActionCut extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				context.copy(self.value.data.subList(beginIndex, endIndex + 1));
				context.history.apply(context,
						new ChangeArray(self.value, beginIndex, endIndex - beginIndex + 1, ImmutableList.of())
				);
				context.history.finishChange(context);
				return true;
			}

		}

		@Action.StaticID(id = "paste")
		private class ActionPaste extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final List<Atom> atoms = context.uncopy(((MiddleArray) self.value.middle()).type);
				if (atoms.isEmpty())
					return false;
				context.history.apply(context,
						new ChangeArray(self.value, beginIndex, endIndex - beginIndex + 1, atoms)
				);
				context.history.finishChange(context);
				return true;
			}
		}

		@Action.StaticID(id = "gather_next")
		private class ActionGatherNext extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final int newIndex = Math.min(self.value.data.size() - 1, endIndex + 1);
				if (endIndex == newIndex)
					return false;
				setEnd(context, newIndex);
				return true;
			}

		}

		@Action.StaticID(id = "release_next")
		private class ActionReleaseNext extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final int newIndex = Math.max(beginIndex, endIndex - 1);
				if (endIndex == newIndex)
					return false;
				setEnd(context, newIndex);
				return true;
			}
		}

		@Action.StaticID(id = "gather_previous")
		private class ActionGatherPrevious extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final int newIndex = Math.max(0, beginIndex - 1);
				if (beginIndex == newIndex)
					return false;
				setBegin(context, newIndex);
				return true;
			}
		}

		@Action.StaticID(id = "release_previous")
		private class ActionReleasePrevious extends ActionBase {
			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final int newIndex = Math.min(endIndex, beginIndex + 1);
				if (beginIndex == newIndex)
					return false;
				setBegin(context, newIndex);
				return true;
			}
		}

		@Action.StaticID(id = "move_before")
		private class ActionMoveBefore extends ActionBase {
			@Override
			public boolean run(final Context context) {
				if (beginIndex == 0)
					return false;
				int index = beginIndex;
				final List<Atom> atoms = ImmutableList.copyOf(self.value.data.subList(index, endIndex + 1));
				context.history.apply(context, new ChangeArray(self.value, index, atoms.size(), ImmutableList.of()));
				setBegin(context, --index);
				context.history.apply(context, new ChangeArray(self.value, index, 0, atoms));
				ArraySelection.this.leadFirst = true;
				setRange(context, index, index + atoms.size() - 1);
				return true;
			}
		}

		@Action.StaticID(id = "move_after")
		private class ActionMoveAfter extends ActionBase {
			@Override
			public boolean run(final Context context) {
				if (endIndex == self.value.data.size() - 1)
					return false;
				int index = beginIndex;
				final List<Atom> atoms = ImmutableList.copyOf(self.value.data.subList(index, endIndex + 1));
				context.history.apply(context, new ChangeArray(self.value, index, atoms.size(), ImmutableList.of()));
				setPosition(context, ++index);
				context.history.apply(context, new ChangeArray(self.value, index, 0, atoms));
				ArraySelection.this.leadFirst = false;
				setRange(context, index, index + atoms.size() - 1);
				return true;
			}
		}

		@Action.StaticID(id = "window")
		private class ActionWindow extends ActionBase {
			@Override
			public boolean run(final Context context) {
				final Atom root = self.value.data.get(beginIndex);
				if (root.visual.selectDown(context)) {
					context.setAtomWindow(root);
					return true;
				}
				return false;
			}
		}

		@Action.StaticID(id = "prefix")
		private class ActionPrefix extends ActionBase {
			private final boolean leadFirst;

			public ActionPrefix(final boolean leadFirst) {
				this.leadFirst = leadFirst;
			}

			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final int index = leadFirst ? beginIndex : endIndex;
				final Atom old = self.value.data.get(index);
				final Atom gap = context.syntax.prefixGap.create();
				context.history.apply(context, new ChangeArray(self.value, index, 1, ImmutableList.of(gap)));
				context.history.apply(context,
						new ChangeArray((ValueArray) gap.data.get("value"), 0, 0, ImmutableList.of(old))
				);
				gap.data.get("gap").selectDown(context);
				return true;
			}
		}

		@Action.StaticID(id = "suffix")
		private class ActionSuffix extends ActionBase {
			private final boolean leadFirst;

			public ActionSuffix(final boolean leadFirst) {
				this.leadFirst = leadFirst;
			}

			@Override
			public boolean run(final Context context) {
				context.history.finishChange(context);
				final int index = leadFirst ? beginIndex : endIndex;
				final Atom old = self.value.data.get(index);
				final Atom gap = context.syntax.suffixGap.create(false);
				context.history.apply(context, new ChangeArray(self.value, index, 1, ImmutableList.of(gap)));
				context.history.apply(context,
						new ChangeArray((ValueArray) gap.data.get("value"), 0, 0, ImmutableList.of(old))
				);
				gap.data.get("gap").selectDown(context);
				return true;
			}
		}
	}

	private static class ArraySelectionState implements SelectionState {
		private final ValueArray value;
		private final int start;
		private final int end;
		private final boolean leadFirst;

		private ArraySelectionState(final ValueArray value, final boolean leadFirst, final int start, final int end) {
			this.value = value;
			this.leadFirst = leadFirst;
			this.start = start;
			this.end = end;
		}

		@Override
		public void select(final Context context) {
			value.select(context, leadFirst, start, end);
		}
	}

	public void select(final Context context, final boolean leadFirst, final int start, final int end) {
		if (hoverable != null)
			hoverable.notifySelected(context, start, end);
		if (selection == null) {
			selection = new ArraySelection(context, this, leadFirst, start, end);
			context.setSelection(selection);
		} else {
			selection.setRange(context, start, end);
		}
	}

	@Override
	public boolean selectDown(final Context context) {
		value.select(context, true, 0, 0);
		return true;
	}

	private class ArrayVisualParent extends Parent {

		private final boolean selectable;

		public ArrayVisualParent(final int index, final boolean selectable) {
			super(VisualArray.this, index);
			this.selectable = selectable;
		}

		private int valueIndex() {
			if (getSeparator().isEmpty())
				return index;
			return index / 2;
		}

		@Override
		public Hoverable hover(final Context context, final Vector point) {
			if (!selectable) {
				if (parent != null)
					return parent.hover(context, point);
				return null;
			}
			if (selection != null && selection.beginIndex == selection.endIndex && selection.beginIndex == valueIndex())
				return null;
			if (hoverable == null) {
				hoverable = new ElementHoverable(context);
			}
			((ElementHoverable) hoverable).setIndex(context, valueIndex());
			return hoverable;
		}

		@Override
		public void firstBrickChanged(final Context context, final Brick firstBrick) {
			if (selection != null && valueIndex() == selection.beginIndex)
				selection.border.setFirst(context, firstBrick);
			if (hoverable != null && valueIndex() == ((ElementHoverable) hoverable).index)
				((ElementHoverable) hoverable).border.setFirst(context, firstBrick);
		}

		@Override
		public void lastBrickChanged(final Context context, final Brick lastBrick) {
			if (selection != null && valueIndex() == selection.endIndex)
				selection.border.setLast(context, lastBrick);
			if (hoverable != null && valueIndex() == ((ElementHoverable) hoverable).index)
				((ElementHoverable) hoverable).border.setLast(context, lastBrick);
		}
	}

	@Override
	public void compact(final Context context) {
		super.compact(context);
		ellipsisTags = ellipsisTags.plus(new StateTag("compact"));
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
		emptyTags = emptyTags.plus(new StateTag("compact"));
		if (empty != null)
			empty.tagsChanged(context);
	}

	@Override
	public void expand(final Context context) {
		super.expand(context);
		ellipsisTags = ellipsisTags.minus(new StateTag("compact"));
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
		emptyTags = emptyTags.minus(new StateTag("compact"));
		if (empty != null)
			empty.tagsChanged(context);
	}

	@Override
	public Hoverable hover(final Context context, final Vector point) {
		if (empty != null) {
			hoverable = new PlaceholderHoverable(context, empty);
			return hoverable;
		} else if (ellipsis != null) {
			hoverable = new PlaceholderHoverable(context, ellipsis);
			return hoverable;
		} else
			return super.hover(context, point);
	}
}
