package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.visual.*;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.MultiVisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualBorderAttachment;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;

public abstract class VisualArray extends VisualGroup {

	private final ValueArray.Listener dataListener;
	private final ValueArray data;
	private Brick ellipsis = null;

	public VisualArray(final Context context, final ValueArray data, final PSet<Visual.Tag> tags) {
		super(tags);
		this.data = data;
		dataListener = new ValueArray.Listener() {

			@Override
			public void changed(final Context context, final int index, final int remove, final List<Node> add) {
				change(context, index, remove, add);
			}
		};
		data.addListener(dataListener);
		coreChange(context, 0, 0, data.get());
		data.visual = this;
	}

	private void change(final Context context, final int index, final int remove, final List<Node> add) {
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
				final Visual visual = parent.getTarget();
				if (visual == this) {
					fixDeepSelectionIndex = ((ArrayVisualParent) parent).valueIndex();
					break;
				}
				parent = visual.parent();
			}
		}
		if (hoverable == null && context.hover != null) {
			VisualParent parent = context.hover.part().parent();
			while (parent != null) {
				final Visual visual = parent.getTarget();
				if (visual == this) {
					fixDeepHoverIndex = ((ArrayVisualParent) parent).valueIndex();
					break;
				}
				parent = visual.parent();
			}
		}

		coreChange(context, index, remove, add);

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
			if (data.get().isEmpty())
				parent.selectUp(context);
			else {
				if (oldSelectionBeginIndex >= index + remove)
					selection.setBegin(context, oldSelectionBeginIndex - remove + add.size());
				else if (oldSelectionBeginIndex >= index)
					selection.setBegin(context, Math.min(data.get().size() - 1, index + Math.max(0, add.size() - 1)));
				if (oldSelectionEndIndex >= index + remove)
					selection.setEnd(context, oldSelectionEndIndex - remove + add.size());
				else if (oldSelectionEndIndex >= index)
					selection.setEnd(context, Math.min(data.get().size() - 1, index + Math.max(0, add.size() - 1)));
			}
		} else if (fixDeepSelectionIndex != null) {
			if (data.get().isEmpty())
				parent.selectUp(context);
			else if (fixDeepSelectionIndex >= index && fixDeepSelectionIndex < index + remove) {
				final int newIndex = Math.min(data.get().size() - 1, index + Math.max(0, add.size() - 1));
				select(context, newIndex, newIndex);
			}
		}
	}

	private void coreChange(final Context context, final int index, final int remove, final List<Node> add) {
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
				children.get(0).changeTags(context, new Visual.TagsChange().remove(new Visual.PartTag("first")));
			if (retagLast)
				last(children).changeTags(context, new Visual.TagsChange().remove(new Visual.PartTag("last")));
		}

		// Remove
		remove(context, visualIndex, visualRemove);

		// Add
		final PSet<Visual.Tag> tags = tags(context);
		int addIndex = visualIndex;
		final Consumer<Integer> addSeparator = addAt -> {
			final ChildGroup group = new ChildGroup(HashTreePSet.empty(), false);
			for (final FrontSymbol fix : getSeparator())
				group.add(context, fix.createVisual(context, tags.plus(new Visual.PartTag("separator"))));
			super.add(context, group, addAt);
		};
		for (final Node node : add) {
			if (!getSeparator().isEmpty() && addIndex > 0)
				addSeparator.accept(addIndex++);
			final ChildGroup group = new ChildGroup(HashTreePSet.empty(), true);
			for (final FrontSymbol fix : getPrefix())
				group.add(context, fix.createVisual(context, tags.plus(new Visual.PartTag("prefix"))));
			final Visual nodeVisual = node.createVisual(context);
			final int addIndex2 = addIndex;
			group.add(context, new VisualPart(tags.plus(new Visual.PartTag("nested"))) {
				@Override
				public void setParent(final VisualParent parent) {
					nodeVisual.setParent(parent);
				}

				@Override
				public VisualParent parent() {
					return nodeVisual.parent();
				}

				@Override
				public boolean selectDown(final Context context) {
					return nodeVisual.selectDown(context);
				}

				@Override
				public void select(final Context context) {
					nodeVisual.select(context);
				}

				@Override
				public void selectUp(final Context context) {
					VisualArray.this.select(context, addIndex2, addIndex2);
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
				public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
						final Context context, final TagsChange change
				) {
					return nodeVisual.getPropertiesForTagsChange(context, change);
				}

				@Override
				public void destroy(final Context context) {
					nodeVisual.destroy(context);
				}

				@Override
				public boolean isAt(final Value value) {
					return VisualArray.this.isAt(value);
				}

				@Override
				public void tagsChanged(final Context context) {

				}
			});
			for (final FrontSymbol fix : getSuffix())
				group.add(context, fix.createVisual(context, tags.plus(new Visual.PartTag("suffix"))));
			super.add(context, group, addIndex++);
		}
		if (!getSeparator().isEmpty() && addIndex < children.size()) {
			addSeparator.accept(addIndex);
		}

		// Cleanup
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new Visual.TagsChange().add(new Visual.PartTag("first")));
			if (retagLast)
				last(children).changeTags(context, new Visual.TagsChange().add(new Visual.PartTag("last")));
		}
	}

	@Override
	public void destroy(final Context context) {
		data.removeListener(dataListener);
		data.visual = null;
		super.destroy(context);
	}

	@Override
	public boolean selectDown(final Context context) {
		if (children.isEmpty()) {
			final List<FreeNodeType> childTypes =
					context.syntax.getLeafTypes(((MiddleArrayBase) data.middle()).type).collect(Collectors.toList());
			final Node element;
			if (childTypes.size() == 1) {
				element = childTypes.get(0).create(context.syntax);
			} else {
				element = context.syntax.gap.create();
			}
			context.history.apply(context, new ChangeArray(data, 0, 0, ImmutableList.of(element)));
		}
		select(context, 0, 0);
		return true;
	}

	@Override
	protected VisualParent createParent(final int index) {
		final boolean selectable = ((ChildGroup) children.get(index)).selectable;
		return new ArrayVisualParent(index, selectable);
	}

	protected abstract boolean tagLast();

	protected abstract boolean tagFirst();

	public abstract int ellipsizeThreshold();

	protected abstract Symbol ellipsis();

	private class ChildGroup extends VisualGroup {

		private final boolean selectable;

		public ChildGroup(final PSet<Visual.Tag> tags, final boolean selectable) {
			super(tags);
			this.selectable = selectable;
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
		return tags(context).plus(new PartTag("ellipsis"));
	}

	private Brick createEllipsis(final Context context) {
		if (ellipsis != null)
			return null;
		ellipsis = ellipsis().createBrick(context, new BrickInterface() {
			@Override
			public VisualPart getVisual() {
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
				return VisualArray.this.getAlignment(style.alignment);
			}

			@Override
			public Set<Tag> getTags(final Context context) {
				return ellipsisTags(context);
			}
		});
		return ellipsis;
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (data.parent.node().getVisual().depth >= ellipsizeThreshold()) {
			return createEllipsis(context);
		} else
			return super.createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		if (data.parent.node().getVisual().depth >= ellipsizeThreshold()) {
			return createEllipsis(context);
		} else
			return super.createLastBrick(context);
	}

	@Override
	public void tagsChanged(final Context context) {
		super.tagsChanged(context);
		if (ellipsis != null) {
			final Style.Baked style = context.getStyle(ellipsisTags(context));
			ellipsis.setStyle(context, style);
		}
	}

	private ArrayHoverable hoverable;

	private class ArrayHoverable extends Hoverable {
		private int index;
		VisualBorderAttachment border;

		public ArrayHoverable(final Context context) {
			border = new VisualBorderAttachment(context, getStyle(context).obbox);
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
			select(context, index, index);
		}

		@Override
		public VisualNodeType node() {
			if (VisualArray.this.parent == null)
				return null;
			return VisualArray.this.parent.getNodeVisual();
		}

		@Override
		public VisualPart part() {
			return VisualArray.this;
		}

		@Override
		public void globalTagsChanged(final Context context) {
			border.setStyle(context, getStyle(context).obbox);
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

		public ArraySelection(final Context context, final int start, final int end) {
			border = new BorderAttachment(context);
			border.setStyle(context, getStyle(context).obbox);
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
			setBegin(context, start);
			setEnd(context, end);
			context.actions.put(this, ImmutableList.of(new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					children.get(beginIndex).selectDown(context);
				}

				@Override
				public String getName() {
					return "enter";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					parent.selectUp(context);
				}

				@Override
				public String getName() {
					return "exit";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					setPosition(context, Math.min(data.get().size() - 1, endIndex + 1));
				}

				@Override
				public String getName() {
					return "next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
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
							new ChangeArray(data, beginIndex, endIndex - beginIndex + 1, ImmutableList.of())
					);
				}

				@Override
				public String getName() {
					return "delete";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.apply(context,
							new ChangeArray(data, beginIndex, 0, ImmutableList.of(context.syntax.gap.create()))
					);
				}

				@Override
				public String getName() {
					return "insert_before";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.apply(context,
							new ChangeArray(data, beginIndex + 1, 0, ImmutableList.of(context.syntax.gap.create()))
					);
				}

				@Override
				public String getName() {
					return "insert_after";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.copy(data.get().subList(beginIndex, endIndex + 1));
				}

				@Override
				public String getName() {
					return "copy";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.history.finishChange(context);
					context.copy(data.get().subList(beginIndex, endIndex + 1));
					context.history.apply(context,
							new ChangeArray(data, beginIndex, endIndex - beginIndex + 1, ImmutableList.of())
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
					final List<Node> nodes = context.uncopy(((MiddleArray) data.middle()).type);
					if (nodes.isEmpty())
						return;
					context.history.apply(context, new ChangeArray(data, beginIndex, endIndex - beginIndex + 1, nodes));
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
					setEnd(context, Math.min(data.get().size() - 1, endIndex + 1));
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
					final List<Node> nodes = ImmutableList.copyOf(data.get().subList(index, endIndex + 1));
					context.history.apply(context, new ChangeArray(data, index, nodes.size(), ImmutableList.of()));
					setBegin(context, --index);
					context.history.apply(context, new ChangeArray(data, index, 0, nodes));
					setBegin(context, index);
					setEnd(context, index + nodes.size() - 1);
				}

				@Override
				public String getName() {
					return "move_before";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					if (endIndex == data.get().size() - 1)
						return;
					int index = beginIndex;
					final List<Node> nodes = ImmutableList.copyOf(data.get().subList(index, endIndex + 1));
					context.history.apply(context, new ChangeArray(data, index, nodes.size(), ImmutableList.of()));
					setPosition(context, ++index);
					context.history.apply(context, new ChangeArray(data, index, 0, nodes));
					setBegin(context, index);
					setEnd(context, index + nodes.size() - 1);
				}

				@Override
				public String getName() {
					return "move_after";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {
					context.window(data.data.get(beginIndex));
				}

				@Override
				public String getName() {
					return "window";
				}
			}));
		}

		private void setEnd(final Context context, final int index) {
			endIndex = index;
			adapter.setLast(context, children.get(visualIndex(index)));
		}

		private void setBegin(final Context context, final int index) {
			beginIndex = index;
			adapter.setFirst(context, children.get(visualIndex(index)));
		}

		private void setPosition(final Context context, final int index) {
			setEnd(context, index);
			setBegin(context, index);
		}

		@Override
		public void clear(final Context context) {
			adapter.destroy(context);
			border.destroy(context);
			selection = null;
			context.actions.remove(this);
		}

		@Override
		public void addBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			adapter.addListener(context, listener);
			final Brick first = getVisual().getFirstBrick(context);
			final Brick last = getVisual().getLastBrick(context);
			if (first != null)
				listener.firstChanged(context, first);
			if (last != null)
				listener.lastChanged(context, last);
		}

		@Override
		public void removeBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			adapter.removeListener(context, listener);
		}

		@Override
		public VisualPart getVisual() {
			return children.get(beginIndex);
		}

		@Override
		public SelectionState saveState() {
			return new ArraySelectionState(data, beginIndex, endIndex);
		}

		@Override
		public Path getPath() {
			return data.getPath().add(String.valueOf(beginIndex));
		}

		@Override
		public void globalTagsChanged(final Context context) {
			border.setStyle(context, getStyle(context).obbox);
		}
	}

	private static class ArraySelectionState implements SelectionState {
		private final ValueArray value;
		private final int start;
		private final int end;

		private ArraySelectionState(final ValueArray value, final int start, final int end) {
			this.value = value;
			this.start = start;
			this.end = end;
		}

		@Override
		public void select(final Context context) {
			((VisualArray) value.visual).select(context, start, end);
		}
	}

	@Override
	public boolean isAt(final Value value) {
		return data == value;
	}

	public void select(final Context context, final int start, final int end) {
		if (hoverable != null && hoverable.index >= start && hoverable.index <= end) {
			context.clearHover();
		}
		if (selection == null) {
			selection = new ArraySelection(context, start, end);
			context.setSelection(selection);
		} else {
			selection.setBegin(context, start);
			selection.setEnd(context, end);
		}
	}

	@Override
	public void select(final Context context) {
		select(context, 0, 0);
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
		public void selectUp(final Context context) {
			select(context, valueIndex(), valueIndex());
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
			final Brick next = super.createNextBrick(context);
			if (selection != null && valueIndex() == selection.beginIndex)
				selection.adapter.notifyPreviousBrickPastEdge(context, next);
			if (hoverable != null && valueIndex() == hoverable.index)
				hoverable.border.notifyPreviousBrickPastEdge(context, next);
			return next;
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
}
