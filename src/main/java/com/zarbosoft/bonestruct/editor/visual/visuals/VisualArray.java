package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.visual.*;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.MultiVisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.last;

public abstract class VisualArray extends VisualGroup implements VisualLeaf {
	private PSet<Tag> tags;
	private PSet<Tag> ellipsisTags;
	private final ValueArray.Listener dataListener;
	private final ValueArray value;
	private Brick ellipsis = null;

	public VisualArray(
			final Context context,
			final VisualParent parent,
			final ValueArray value,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		this.tags = tags;
		ellipsisTags = this.tags.plus(new PartTag("ellipsis"));
		this.value = value;
		dataListener = new ValueArray.Listener() {

			@Override
			public void changed(final Context context, final int index, final int remove, final List<Atom> add) {
				if (ellipsize(context))
					return;
				change(context, index, remove, add);
			}
		};
		value.addListener(dataListener);
		value.visual = this;
		root(context, parent, alignments, depth);
	}

	private void change(final Context context, final int index, final int remove, final List<Atom> add) {
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
				if (visual == this) {
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
				if (visual == this) {
					fixDeepHoverIndex = ((ArrayVisualParent) parent).valueIndex();
					break;
				}
				parent = visual.parent();
			}
		}

		coreChange(context, index, remove, add);
		if (!add.isEmpty()) {
			final int layIndex = visualIndex(index);
			context.idleLayBricks(parent,
					layIndex,
					visualIndex(index + add.size()) - layIndex,
					children.size(),
					i -> children.get(i).getFirstBrick(context),
					i -> children.get(i).getLastBrick(context),
					i -> children.get(i).createFirstBrick(context)
			);
		}

		if (hoverable != null) {
			if (hoverable.index >= index + remove) {
				hoverable.setIndex(context, hoverable.index - remove + add.size());
			} else if (hoverable.index >= index) {
				context.clearHover();
			}
		} else if (fixDeepHoverIndex != null && fixDeepHoverIndex >= index && fixDeepHoverIndex < index + remove) {
			context.clearHover();
		}
		if (oldSelectionBeginIndex != null) {
			if (value.data.isEmpty())
				value.parent.selectUp(context);
			else {
				if (oldSelectionBeginIndex >= index + remove)
					selection.setBegin(context, oldSelectionBeginIndex - remove + add.size());
				else if (oldSelectionBeginIndex >= index)
					selection.setBegin(context, Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
				if (oldSelectionEndIndex >= index + remove)
					selection.setEnd(context, oldSelectionEndIndex - remove + add.size());
				else if (oldSelectionEndIndex >= index)
					selection.setEnd(context, Math.min(value.data.size() - 1, index + Math.max(0, add.size() - 1)));
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
		int addIndex = visualIndex;
		final Consumer<Integer> addSeparator = addAt -> {
			final ChildGroup group = new ChildGroup(context, new ArrayVisualParent(addAt, false), depth());
			for (int fixIndex = 0; fixIndex < getSeparator().size(); ++fixIndex) {
				final FrontSymbol fix = getSeparator().get(fixIndex);
				group.add(context, fix.createVisual(context, group.createParent(fixIndex), tags, alignments, depth()));
			}
			if (parent.atomVisual().compact)
				group.compact(context);
			super.add(context, group, addAt);
		};
		if (!add.isEmpty()) {
			for (final Atom atom : add) {
				if (!getSeparator().isEmpty() && addIndex > 0)
					addSeparator.accept(addIndex++);
				final ChildGroup group = new ChildGroup(context, new ArrayVisualParent(addIndex, true), depth());
				int groupIndex = 0;
				for (final FrontSymbol fix : getPrefix())
					group.add(context,
							fix.createVisual(context, group.createParent(groupIndex++), tags, alignments, depth())
					);
				final VisualAtom nodeVisual =
						(VisualAtom) atom.createVisual(context, group.createParent(groupIndex++), alignments, depth());
				final int addIndex2 = addIndex;
				group.add(context, new Visual() {
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
					public boolean canCreateBricks(final Context context) {
						return nodeVisual.canCreateBricks(context);
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
							final int depth
					) {
						nodeVisual.root(context, parent, alignments, depth);
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
					group.add(context,
							fix.createVisual(context, group.createParent(groupIndex++), tags, alignments, depth())
					);
				if (parent.atomVisual().compact)
					group.compact(context);
				super.add(context, group, addIndex++);
			}
			if (!getSeparator().isEmpty() && addIndex < children.size()) {
				addSeparator.accept(addIndex);
			}
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
		if (ellipsis != null)
			return Stream.of(ellipsis);
		return super.streamBricks();
	}

	private class ChildGroup extends VisualGroup {

		public ChildGroup(
				final Context context, final VisualParent parent, final int depth
		) {
			super(context, parent, depth);
		}

		@Override
		public Brick createFirstBrick(final Context context) {
			final Brick out = super.createFirstBrick(context);
			if (selection != null) {
				if (selection.beginIndex == ((ArrayVisualParent) parent).valueIndex())
					selection.border.setFirst(context, out);
				if (selection.endIndex == ((ArrayVisualParent) parent).valueIndex())
					selection.adapter.notifySeedBrick(context, out);
			}
			if (hoverable != null && hoverable.index == ((ArrayVisualParent) parent).valueIndex()) {
				hoverable.border.setFirst(context, out);
				hoverable.border.notifySeedBrick(context, out);
			}
			return out;
		}

		@Override
		public Brick createLastBrick(final Context context) {
			final Brick out = super.createLastBrick(context);
			if (selection != null) {
				if (selection.beginIndex == ((ArrayVisualParent) parent).valueIndex())
					selection.adapter.notifySeedBrick(context, out);
				if (selection.endIndex == ((ArrayVisualParent) parent).valueIndex())
					selection.border.setLast(context, out);
			}
			if (hoverable != null && hoverable.index == ((ArrayVisualParent) parent).valueIndex()) {
				hoverable.border.setLast(context, out);
				hoverable.border.notifySeedBrick(context, out);
			}
			return out;
		}
	}

	protected abstract List<FrontSymbol> getPrefix();

	protected abstract List<FrontSymbol> getSeparator();

	protected abstract List<FrontSymbol> getSuffix();

	private Set<Tag> ellipsisTags(final Context context) {
		return context.globalTags.plusAll(tags).plus(new PartTag("ellipsis"));
	}

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
		return ellipsis;
	}

	private boolean ellipsize(final Context context) {
		if (!context.window)
			return false;
		return parent.atomVisual().depth >= context.syntax.ellipsizeThreshold;
	}

	@Override
	public boolean canCreateBricks(final Context context) {
		if (ellipsize(context))
			return true;
		else
			return super.canCreateBricks(context);
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		if (ellipsize(context)) {
			if (ellipsis != null)
				return ellipsis;
			else
				return createEllipsis(context);
		} else
			return super.createOrGetFirstBrick(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (ellipsize(context)) {
			return createEllipsis(context);
		} else
			return super.createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		if (ellipsize(context)) {
			return createEllipsis(context);
		} else
			return super.createLastBrick(context);
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		if (ellipsize(context))
			return ellipsis;
		return super.getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		if (ellipsize(context))
			return ellipsis;
		return super.getLastBrick(context);
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		this.parent = parent;
		if (ellipsize(context)) {
			if (!children.isEmpty()) {
				remove(context, 0, children.size());
			}
			super.root(context, parent, alignments, depth);
			context.idleLayBricks(parent, 0, 1, 1, null, null, i -> createEllipsis(context));
		} else {
			if (ellipsis != null)
				ellipsis.destroy(context);
			super.root(context, parent, alignments, depth);
			if (!value.data.isEmpty() && children.isEmpty()) {
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
		value.removeListener(dataListener);
		value.visual = null;
		super.uproot(context, root);
		children.clear();
	}

	public void tagsChanged(final Context context) {
		if (ellipsis != null) {
			ellipsis.tagsChanged(context);
		}
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
		tagsChanged(context);
		super.changeTags(context, change);
	}

	private ArrayHoverable hoverable;

	private class ArrayHoverable extends Hoverable {
		private int index;
		VisualBorderAttachment border;

		public ArrayHoverable(final Context context) {
			border = new VisualBorderAttachment(context, getBorderStyle(context, tags).obbox);
		}

		public void setIndex(final Context context, final int index) {
			this.index = index;
			border.setFirst(context, children.get(visualIndex(index)));
			border.setLast(context, children.get(visualIndex(index)));
		}

		@Override
		public void clear(final Context context) {
			border.destroy(context);
			hoverable = null;
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
		public Visual visual() {
			return VisualArray.this;
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			border.setStyle(context, getBorderStyle(context, tags).obbox);
		}
	}

	private int visualIndex(final int valueIndex) {
		if (getSeparator().isEmpty())
			return valueIndex;
		else
			return valueIndex * 2;
	}

	public ArraySelection selection;

	public class ArraySelection extends Selection {
		MultiVisualAttachmentAdapter adapter;
		BorderAttachment border;
		public int beginIndex;
		public int endIndex;
		public boolean leadFirst;

		public ArraySelection(final Context context, final boolean leadFirst, final int start, final int end) {
			border = new BorderAttachment(context);
			border.setStyle(context, getBorderStyle(context, tags).obbox);
			adapter = new MultiVisualAttachmentAdapter(context);
			adapter.addListener(context, new VisualAttachmentAdapter.BoundsListener() {
				@Override
				public void firstChanged(final Context context, final Brick brick) {
					border.setFirst(context, brick);
				}

				@Override
				public void lastChanged(final Context context, final Brick brick) {
					border.setLast(context, brick);
				}
			});
			this.leadFirst = leadFirst;
			setRange(context, start, end);
			context.addActions(this, ImmutableList.of(new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					value.data.get(beginIndex).visual.selectDown(context);
				}

				@Override
				public String getName() {
					return "enter";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					value.parent.selectUp(context);
				}

				@Override
				public String getName() {
					return "exit";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					ArraySelection.this.leadFirst = true;
					setPosition(context, Math.min(value.data.size() - 1, endIndex + 1));
				}

				@Override
				public String getName() {
					return "next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					ArraySelection.this.leadFirst = true;
					setPosition(context, Math.max(0, beginIndex - 1));
				}

				@Override
				public String getName() {
					return "previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.apply(context,
							new ChangeArray(value, beginIndex, endIndex - beginIndex + 1, ImmutableList.of())
					);
				}

				@Override
				public String getName() {
					return "delete";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final Atom gap = context.syntax.gap.create();
					context.history.apply(context, new ChangeArray(value, beginIndex, 0, ImmutableList.of(gap)));
					gap.data.get("gap").selectDown(context);
				}

				@Override
				public String getName() {
					return "insert_before";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final Atom gap = context.syntax.gap.create();
					context.history.apply(context, new ChangeArray(value, endIndex + 1, 0, ImmutableList.of(gap)));
					gap.data.get("gap").selectDown(context);
				}

				@Override
				public String getName() {
					return "insert_after";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.copy(value.data.subList(beginIndex, endIndex + 1));
				}

				@Override
				public String getName() {
					return "copy";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					context.copy(value.data.subList(beginIndex, endIndex + 1));
					context.history.apply(context,
							new ChangeArray(value, beginIndex, endIndex - beginIndex + 1, ImmutableList.of())
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
					final List<Atom> atoms = context.uncopy(((MiddleArray) value.middle()).type);
					if (atoms.isEmpty())
						return;
					context.history.apply(context,
							new ChangeArray(value, beginIndex, endIndex - beginIndex + 1, atoms)
					);
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
					setEnd(context, Math.min(value.data.size() - 1, endIndex + 1));
				}

				@Override
				public String getName() {
					return "gather_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					setEnd(context, Math.max(beginIndex, endIndex - 1));
				}

				@Override
				public String getName() {
					return "release_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					setBegin(context, Math.max(0, beginIndex - 1));
				}

				@Override
				public String getName() {
					return "gather_previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					setBegin(context, Math.min(endIndex, beginIndex + 1));
				}

				@Override
				public String getName() {
					return "release_previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (beginIndex == 0)
						return;
					int index = beginIndex;
					final List<Atom> atoms = ImmutableList.copyOf(value.data.subList(index, endIndex + 1));
					context.history.apply(context, new ChangeArray(value, index, atoms.size(), ImmutableList.of()));
					setBegin(context, --index);
					context.history.apply(context, new ChangeArray(value, index, 0, atoms));
					ArraySelection.this.leadFirst = true;
					setRange(context, index, index + atoms.size() - 1);
				}

				@Override
				public String getName() {
					return "move_before";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (endIndex == value.data.size() - 1)
						return;
					int index = beginIndex;
					final List<Atom> atoms = ImmutableList.copyOf(value.data.subList(index, endIndex + 1));
					context.history.apply(context, new ChangeArray(value, index, atoms.size(), ImmutableList.of()));
					setPosition(context, ++index);
					context.history.apply(context, new ChangeArray(value, index, 0, atoms));
					ArraySelection.this.leadFirst = false;
					setRange(context, index, index + atoms.size() - 1);
				}

				@Override
				public String getName() {
					return "move_after";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					final Atom root = value.data.get(beginIndex);
					if (root.visual.selectDown(context))
						context.setAtomWindow(root);
				}

				@Override
				public String getName() {
					return "window";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					final int index = leadFirst ? beginIndex : endIndex;
					final Atom old = value.data.get(index);
					final Atom gap = context.syntax.prefixGap.create();
					context.history.apply(context, new ChangeArray(value, index, 1, ImmutableList.of(gap)));
					context.history.apply(context,
							new ChangeArray((ValueArray) gap.data.get("value"), 0, 0, ImmutableList.of(old))
					);
					gap.data.get("gap").selectDown(context);
				}

				@Override
				public String getName() {
					return "prefix";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					final int index = leadFirst ? beginIndex : endIndex;
					final Atom old = value.data.get(index);
					final Atom gap = context.syntax.suffixGap.create(false);
					context.history.apply(context, new ChangeArray(value, index, 1, ImmutableList.of(gap)));
					context.history.apply(context,
							new ChangeArray((ValueArray) gap.data.get("value"), 0, 0, ImmutableList.of(old))
					);
					gap.data.get("gap").selectDown(context);
				}

				@Override
				public String getName() {
					return "suffix";
				}
			}));
		}

		private void setEndInternal(final Context context, final int index) {
			endIndex = index;
			adapter.setLast(context, children.get(visualIndex(index)));
		}

		private void setBeginInternal(final Context context, final int index) {
			beginIndex = index;
			adapter.setFirst(context, children.get(visualIndex(index)));
		}

		private void setBegin(final Context context, final int index) {
			leadFirst = true;
			setBeginInternal(context, index);
			updateCornerstone(context);
		}

		private void setEnd(final Context context, final int index) {
			leadFirst = false;
			setEndInternal(context, index);
			updateCornerstone(context);
		}

		private void setRange(final Context context, final int begin, final int end) {
			setBeginInternal(context, begin);
			setEndInternal(context, end);
			updateCornerstone(context);
		}

		private void setPosition(final Context context, final int index) {
			setEndInternal(context, index);
			setBeginInternal(context, index);
			updateCornerstone(context);
		}

		private void updateCornerstone(final Context context) {
			context.foreground.setCornerstone(context,
					children.get(visualIndex(leadFirst ? beginIndex : endIndex)).createOrGetFirstBrick(context)
			);
		}

		@Override
		public void clear(final Context context) {
			adapter.destroy(context);
			border.destroy(context);
			selection = null;
			context.removeActions(this);
		}

		@Override
		public Visual getVisual() {
			return children.get(beginIndex);
		}

		@Override
		public SelectionState saveState() {
			return new ArraySelectionState(value, leadFirst, beginIndex, endIndex);
		}

		@Override
		public Path getPath() {
			return value.getPath().add(String.valueOf(beginIndex));
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			border.setStyle(context, getBorderStyle(context, tags).obbox);
			super.tagsChanged(context);
		}

		@Override
		public PSet<Tag> getTags(final Context context) {
			return tags;
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
		if (hoverable != null && hoverable.index >= start && hoverable.index <= end) {
			context.clearHover();
		}
		if (selection == null) {
			selection = new ArraySelection(context, leadFirst, start, end);
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
			if (selection != null && selection.beginIndex == valueIndex() && selection.endIndex == valueIndex()) {
				if (hoverable != null)
					context.clearHover();
				return null;
			}
			if (hoverable == null) {
				hoverable = new ArrayHoverable(context);
			}
			hoverable.setIndex(context, valueIndex());
			return hoverable;
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			final Brick previous = super.createPreviousBrick(context);
			if (selection != null && valueIndex() == selection.beginIndex)
				selection.adapter.notifyPreviousBrickPastEdge(context, previous);
			if (hoverable != null && valueIndex() == hoverable.index)
				hoverable.border.notifyPreviousBrickPastEdge(context, previous);
			return previous;
		}

		@Override
		public Brick createNextBrick(final Context context) {
			final Brick next = super.createNextBrick(context);
			if (selection != null && valueIndex() == selection.endIndex)
				selection.adapter.notifyNextBrickPastEdge(context, next);
			if (hoverable != null && valueIndex() == hoverable.index)
				hoverable.border.notifyNextBrickPastEdge(context, next);
			return next;
		}
	}

	@Override
	public boolean canExpand() {
		if (ellipsis == null)
			throw new AssertionError();
		return parent.atomVisual().compact;
	}

	@Override
	public boolean canCompact() {
		if (ellipsis == null)
			throw new AssertionError();
		return !parent.atomVisual().compact;
	}

	@Override
	public void compact(final Context context) {
		super.compact(context);
		ellipsisTags = ellipsisTags.plus(new StateTag("compact"));
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
	}

	@Override
	public void expand(final Context context) {
		super.expand(context);
		ellipsisTags = ellipsisTags.minus(new StateTag("compact"));
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
	}
}
