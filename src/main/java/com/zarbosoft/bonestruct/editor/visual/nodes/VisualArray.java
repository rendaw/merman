package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.MultiVisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualBorderAttachment;
import com.zarbosoft.bonestruct.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.front.FrontConstantPart;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;
import static java.io.File.separator;

public abstract class VisualArray extends VisualGroup {

	private final ValueArray.Listener dataListener;
	private final ValueArray data;

	public VisualArray(final Context context, final ValueArray data, final Set<Visual.Tag> tags) {
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
		int visualIndex = index;
		int visualRemove = remove;
		int visualAdd = add.size();
		if (!separator.isEmpty()) {
			visualIndex = index == 0 ? 0 : visualIndex * 2 - 1;
			visualAdd = children.isEmpty() ? visualAdd * 2 - 1 : visualAdd * 2;
			visualRemove = Math.min(visualRemove * 2, children.size());
		}

		// Prep to fix selection if deep under an element
		Integer fixDeepSelectionIndex = null;
		if (context.selection != null) {
			VisualParent parent = context.selection.getVisual().parent();
			while (parent != null) {
				final Visual visual = parent.getTarget();
				if (visual == this) {
					fixDeepSelectionIndex = ((ArrayVisualParent) parent).index;
					break;
				}
				parent = visual.parent();
			}
		}

		coreChange(context, index, remove, add);

		if (hoverable != null) {
			if (hoverable.index > visualIndex + visualRemove) {
				hoverable.setIndex(context, hoverable.index - visualRemove + visualAdd);
			} else if (hoverable.index >= visualIndex) {
				context.clearHover();
			}
		}
		if (selection != null) {
			if (children.isEmpty())
				parent.selectUp(context);
			else {
				if (selection.beginIndex > visualIndex + visualRemove)
					selection.setBegin(context, selection.beginIndex - visualRemove + visualAdd);
				else if (selection.beginIndex >= visualIndex)
					selection.setBegin(context, Math.min(children.size() - 1, (visualIndex + visualAdd + 1) / 2 * 2));
				if (selection.endIndex > visualIndex + visualRemove)
					selection.setEnd(context, selection.endIndex - visualRemove + visualAdd);
				else if (selection.endIndex >= visualIndex)
					selection.setEnd(context, Math.min(children.size() - 1, (visualIndex + visualAdd + 1) / 2 * 2));
			}
		} else if (fixDeepSelectionIndex != null) {
			if (children.isEmpty())
				parent.selectUp(context);
			else {
				final Integer newIndex;
				if (fixDeepSelectionIndex > visualIndex + visualRemove)
					newIndex = selection.beginIndex - visualRemove + visualAdd;
				else if (fixDeepSelectionIndex >= visualIndex)
					newIndex = Math.min(children.size() - 1, (visualIndex + visualAdd + 1) / 2 * 2);
				else
					newIndex = null;
				if (newIndex != null) {
					select(context, newIndex, newIndex);
				}
			}
		}
	}

	private void coreChange(final Context context, final int index, final int remove, final List<Node> add) {
		int visualIndex = index;
		int visualRemove = remove;
		int visualAdd = add.size();
		if (!separator.isEmpty()) {
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
		final PSet<Visual.Tag> tags = HashTreePSet.from(tags(context));
		int addIndex = visualIndex;
		final Consumer<Integer> addSeparator = addAt -> {
			final ChildGroup group = new ChildGroup(ImmutableSet.of(), false);
			for (final FrontConstantPart fix : getSeparator())
				group.add(context, fix.createVisual(context, tags.plus(new Visual.PartTag("separator"))));
			super.add(context, group, addAt);
		};
		for (final Node node : add) {
			if (addIndex > 0)
				addSeparator.accept(addIndex++);
			final ChildGroup group = new ChildGroup(ImmutableSet.of(), true);
			for (final FrontConstantPart fix : getPrefix())
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
			for (final FrontConstantPart fix : getSuffix())
				group.add(context, fix.createVisual(context, tags.plus(new Visual.PartTag("suffix"))));
			super.add(context, group, addIndex++);
		}
		if (!separator.isEmpty() && addIndex < children.size()) {
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

	private class ChildGroup extends VisualGroup {

		private final boolean selectable;

		public ChildGroup(final Set<Visual.Tag> tags, final boolean selectable) {
			super(tags);
			this.selectable = selectable;
		}

		@Override
		public Brick createFirstBrick(final Context context) {
			final Brick out = super.createFirstBrick(context);
			if (selection != null) {
				if (selection.beginIndex == ((ArrayVisualParent) parent).index)
					selection.border.setFirst(context, out);
				if (selection.endIndex == ((ArrayVisualParent) parent).index)
					selection.adapter.notifySeedBrick(context, out);
			}
			if (hoverable != null && hoverable.index == ((ArrayVisualParent) parent).index) {
				hoverable.border.setFirst(context, out);
				hoverable.border.notifySeedBrick(context, out);
			}
			return out;
		}

		@Override
		public Brick createLastBrick(final Context context) {
			final Brick out = super.createLastBrick(context);
			if (selection != null) {
				if (selection.beginIndex == ((ArrayVisualParent) parent).index)
					selection.adapter.notifySeedBrick(context, out);
				if (selection.endIndex == ((ArrayVisualParent) parent).index)
					selection.border.setLast(context, out);
			}
			if (hoverable != null && hoverable.index == ((ArrayVisualParent) parent).index) {
				hoverable.border.setLast(context, out);
				hoverable.border.notifySeedBrick(context, out);
			}
			return out;
		}
	}

	protected abstract List<FrontConstantPart> getPrefix();

	protected abstract List<FrontConstantPart> getSeparator();

	protected abstract List<FrontConstantPart> getSuffix();

	private ArrayHoverable hoverable;

	private class ArrayHoverable extends Hoverable {
		private int index;
		VisualBorderAttachment border;

		public ArrayHoverable(final Context context) {
			border = new VisualBorderAttachment(context, context.syntax.hoverStyle);
		}

		public void setIndex(final Context context, final int index) {
			this.index = index;
			border.setFirst(context, children.get(index));
			border.setLast(context, children.get(index));
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
		public NodeType.NodeTypeVisual node() {
			if (VisualArray.this.parent == null)
				return null;
			return VisualArray.this.parent.getNodeVisual();
		}

		@Override
		public VisualPart part() {
			return VisualArray.this;
		}
	}

	private ArraySelection selection;

	private class ArraySelection extends Selection {
		MultiVisualAttachmentAdapter adapter;
		BorderAttachment border;
		int beginIndex;
		int endIndex;

		private int valueIndex() {
			if (!getSeparator().isEmpty())
				return beginIndex / 2;
			return beginIndex;
		}

		public ArraySelection(final Context context, final int start, final int end) {
			if (context.display != null) {
				border = new BorderAttachment(context, context.syntax.selectStyle);
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
			}
			setBegin(context, start);
			setEnd(context, end);
			context.actions.put(this, ImmutableList.of(new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "enter";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "exit";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "insert_before";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "insert_after";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "copy";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "cut";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "paste";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "reset_selection";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "gather_next";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "gather_previous";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "move_before";
				}
			}, new Action() {
				@Override
				public void run(final Context context) {

				}

				@Override
				public String getName() {
					return "move_after";
				}
			}));
		}

		private void setEnd(final Context context, final int index) {
			endIndex = index;
			if (context.display != null) {
				adapter.setLast(context, children.get(index));
			}
		}

		private void setBegin(final Context context, final int index) {
			beginIndex = index;
			if (context.display != null) {
				adapter.setFirst(context, children.get(index));
			}
		}

		@Override
		public void clear(final Context context) {
			if (context.display != null) {
				adapter.destroy(context);
				border.destroy(context);
			}
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
			return data.getPath().add(String.valueOf(valueIndex()));
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
		selection = new ArraySelection(context, start, end);
		context.setSelection(selection);
	}

	private class ArrayVisualParent extends Parent {

		private final boolean selectable;

		public ArrayVisualParent(final int index, final boolean selectable) {
			super(VisualArray.this, index);
			this.selectable = selectable;
		}

		@Override
		public void selectUp(final Context context) {
			select(context, getIndex(), getIndex());
		}

		@Override
		public Hoverable hover(final Context context, final Vector point) {
			if (!selectable) {
				if (parent != null)
					return parent.hover(context, point);
				return null;
			}
			if (selection != null && selection.beginIndex == index && selection.endIndex == index) {
				if (hoverable != null)
					context.clearHover();
				return null;
			}
			if (hoverable == null) {
				hoverable = new ArrayHoverable(context);
			}
			hoverable.setIndex(context, index);
			return hoverable;
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			final Brick next = super.createNextBrick(context);
			if (selection != null && index == selection.beginIndex)
				selection.adapter.notifyPreviousBrickPastEdge(context, next);
			if (hoverable != null && index == hoverable.index)
				hoverable.border.notifyPreviousBrickPastEdge(context, next);
			return next;
		}

		@Override
		public Brick createNextBrick(final Context context) {
			final Brick next = super.createNextBrick(context);
			if (selection != null && index == selection.endIndex)
				selection.adapter.notifyNextBrickPastEdge(context, next);
			if (hoverable != null && index == hoverable.index)
				hoverable.border.notifyNextBrickPastEdge(context, next);
			return next;
		}
	}
}
