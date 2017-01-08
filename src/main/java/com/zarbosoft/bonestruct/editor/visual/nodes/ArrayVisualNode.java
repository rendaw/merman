package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.pidgoon.internal.Helper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.io.File.separator;

public abstract class ArrayVisualNode extends GroupVisualNode {

	private final ListChangeListener<Node> dataListener;
	private final ObservableList<Node> data;

	public ArrayVisualNode(final Context context, final ObservableList<Node> nodes, final Set<VisualNode.Tag> tags) {
		super(tags);
		this.data = nodes;
		dataListener = c -> {
			while (c.next()) {
				if (c.wasPermutated()) {
					remove(context, c.getFrom(), c.getRemovedSize());
					add(context, c.getFrom(), nodes.subList(c.getFrom(), c.getTo()));
				} else if (c.wasUpdated()) {
					remove(context, c.getFrom(), c.getTo() - c.getFrom());
					add(context, c.getFrom(), nodes.subList(c.getFrom(), c.getTo()));
				} else {
					remove(context, c.getFrom(), c.getRemovedSize());
					add(context, c.getFrom(), (List<Node>) c.getAddedSubList());
				}
			}
		};
		nodes.addListener(new WeakListChangeListener<>(dataListener));
		add(context, 0, nodes);
	}

	@Override
	public boolean select(final Context context) {
		if (children.isEmpty())
			return false;
		((ArrayVisualNodeParent) children.get(0).parent()).selectDown(context);
		return true;
	}

	@Override
	protected VisualNodeParent createParent(final int index) {
		final boolean selectable = ((ChildGroup) children.get(index)).selectable;
		return new ArrayVisualNodeParent(index, selectable);
	}

	@Override
	public void remove(final Context context, int start, int size) {
		if (!getSeparator().isEmpty()) {
			size += size - 1;
			start *= 2;
		}
		final boolean retagFirst = tagFirst() && start == 0;
		final boolean retagLast = tagLast() && start + size == children.size();
		super.remove(context, start, size);
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new VisualNode.TagsChange().add(new VisualNode.PartTag("first")));
			if (retagLast)
				Helper
						.last(children)
						.changeTags(context, new VisualNode.TagsChange().add(new VisualNode.PartTag("last")));
		}
	}

	protected abstract boolean tagLast();

	protected abstract boolean tagFirst();

	private class ChildGroup extends GroupVisualNode {

		private final boolean selectable;

		public ChildGroup(final Set<VisualNode.Tag> tags, final boolean selectable) {
			super(tags);
			this.selectable = selectable;
		}
	}

	public void add(final Context context, final int start, final List<Node> nodes) {
		final boolean retagFirst = tagFirst() && start == 0;
		final boolean retagLast = tagLast() && start == children.size();
		if (!children.isEmpty()) {
			if (retagFirst)
				children
						.get(0)
						.changeTags(context, new VisualNode.TagsChange().remove(new VisualNode.PartTag("first")));
			if (retagLast)
				Helper
						.last(children)
						.changeTags(context, new VisualNode.TagsChange().remove(new VisualNode.PartTag("last")));
		}
		final PSet<VisualNode.Tag> tags = HashTreePSet.from(tags());
		Helper.enumerate(nodes.stream(), start).forEach(p -> {
			int index = p.first;
			if (p.first > 0 && !separator.isEmpty()) {
				index = index * 2 - 1;
				final ChildGroup group = new ChildGroup(ImmutableSet.of(), false);
				for (final FrontConstantPart fix : getSeparator())
					group.add(context, fix.createVisual(context, tags.plus(new VisualNode.PartTag("separator"))));
				super.add(context, group, index++);
			}
			final ChildGroup group = new ChildGroup(ImmutableSet.of(), true);
			for (final FrontConstantPart fix : getPrefix())
				group.add(context, fix.createVisual(context, tags.plus(new VisualNode.PartTag("prefix"))));
			group.add(context,
					new NestedVisualNodePart(p.second.createVisual(context),
							tags.plus(new VisualNode.PartTag("nested"))
					)
			);
			for (final FrontConstantPart fix : getSuffix())
				group.add(context, fix.createVisual(context, tags.plus(new VisualNode.PartTag("suffix"))));
			super.add(context, group, index);
		});
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new VisualNode.TagsChange().add(new VisualNode.PartTag("first")));
			if (retagLast)
				Helper
						.last(children)
						.changeTags(context, new VisualNode.TagsChange().add(new VisualNode.PartTag("last")));
		}
	}

	protected abstract Map<String, com.zarbosoft.luxemj.grammar.Node> getHotkeys();

	protected abstract List<FrontConstantPart> getPrefix();

	protected abstract List<FrontConstantPart> getSeparator();

	protected abstract List<FrontConstantPart> getSuffix();

	private class ArrayVisualNodeParent extends GroupVisualNodeParent {

		private BorderAttachment border;
		private final boolean selectable;
		private boolean selected = false;

		public ArrayVisualNodeParent(final int index, final boolean selectable) {
			super(ArrayVisualNode.this, index);
			this.selectable = selectable;
		}

		public void selectDown(final Context context) {
			if (selected)
				throw new AssertionError("Already selected");
			else if (border != null) {
				context.clearHover();
			}
			selected = true;
			border = new BorderAttachment(context,
					context.syntax.selectStyle,
					children.get(index).getFirstBrick(context),
					children.get(index).getLastBrick(context)
			);
			context.setSelection(new Context.Selection() {
				@Override
				protected Hotkeys getHotkeys(final Context context) {
					return context.getHotkeys(tags());
				}

				@Override
				public void clear(final Context context) {
					border.destroy(context);
					border = null;
					selected = false;
				}

				@Override
				public Iterable<Context.Action> getActions(final Context context) {
					return ImmutableList.of(new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "enter";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "exit";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "next";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "previous";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "insert-before";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "insert-after";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "copy";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "cut";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "paste";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "reset-selection";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "gather-next";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "gather-previous";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "move-before";
						}
					}, new Context.Action() {
						@Override
						public void run(final Context context) {

						}

						@Override
						public String getName() {
							return "move-after";
						}
					});
				}
			});
		}

		Context.Hoverable hoverable;

		@Override
		public Context.Hoverable hover(final Context context, final Vector point) {
			if (!selectable) {
				if (parent != null)
					return parent.hover(context, point);
				return null;
			}
			if (selected)
				return null;
			if (hoverable != null)
				return hoverable;
			border = new BorderAttachment(context,
					context.syntax.hoverStyle,
					children.get(index).getFirstBrick(context),
					children.get(index).getLastBrick(context)
			);
			hoverable = new Context.Hoverable() {
				@Override
				public void clear(final Context context) {
					border.destroy(context);
					border = null;
					hoverable = null;
				}

				@Override
				public void click(final Context context) {
					selectDown(context);
				}
			};
			return hoverable;
		}

		@Override
		public Brick createNextBrick(final Context context) {
			if (border != null) {
				border.setLast(context, children.get(index).getLastBrick(context));
			}
			return super.createNextBrick(context);
		}
	}
}
