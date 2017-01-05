package com.zarbosoft.bonestruct.editor.visual.nodes.parts;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
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

	public ArrayVisualNode(final Context context, final ObservableList<Node> nodes, final Set<Tag> tags) {
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
	protected VisualNodeParent createParent(final int index) {
		final boolean selectable = ((ChildGroup) children.get(index)).selectable;
		return new GroupVisualNodeParent(this, index) {

			class ArrayHoverable extends Context.Hoverable {

				BorderAttachment borderAttachment;

				ArrayHoverable(final Context context, final int index) {
					borderAttachment = new BorderAttachment(
							context,
							context.syntax.hoverStyle,
							children.get(index).getFirstBrick(context),
							children.get(index).getLastBrick(context)
					);
				}

				@Override
				public void clear(final Context context) {
					borderAttachment.destroy(context);
					hoverable = null;
				}
			}

			ArrayHoverable hoverable;

			@Override
			public Context.Hoverable hover(final Context context) {
				if (!selectable) {
					if (parent != null)
						return parent.hover(context);
					return null;
				}
				if (hoverable != null)
					return hoverable;
				hoverable = new ArrayHoverable(context, index);
				return hoverable;
			}

			@Override
			public Brick createNextBrick(final Context context) {
				final Brick newLast = super.createNextBrick(context);
				if (hoverable != null && newLast != null)
					hoverable.borderAttachment.setLast(context, newLast);
				return newLast;
			}
		};
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
				children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
			if (retagLast)
				Helper.last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
		}
	}

	protected abstract boolean tagLast();

	protected abstract boolean tagFirst();

	private class ChildGroup extends GroupVisualNode {

		private final boolean selectable;

		public ChildGroup(final Set<Tag> tags, final boolean selectable) {
			super(tags);
			this.selectable = selectable;
		}
	}

	public void add(final Context context, final int start, final List<Node> nodes) {
		final boolean retagFirst = tagFirst() && start == 0;
		final boolean retagLast = tagLast() && start == children.size();
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new TagsChange().remove(new PartTag("first")));
			if (retagLast)
				Helper.last(children).changeTags(context, new TagsChange().remove(new PartTag("last")));
		}
		final PSet<Tag> tags = HashTreePSet.from(tags());
		Helper.enumerate(nodes.stream(), start).forEach(p -> {
			int index = p.first;
			if (p.first > 0 && !separator.isEmpty()) {
				index = index * 2 - 1;
				final ChildGroup group = new ChildGroup(ImmutableSet.of(), false);
				for (final FrontConstantPart fix : getSeparator())
					group.add(context, fix.createVisual(context, tags.plus(new PartTag("separator"))));
				super.add(context, group, index++);
			}
			final ChildGroup group = new ChildGroup(ImmutableSet.of(), true);
			for (final FrontConstantPart fix : getPrefix())
				group.add(context, fix.createVisual(context, tags.plus(new PartTag("prefix"))));
			group.add(
					context,
					new NestedVisualNodePart(p.second.createVisual(context), tags.plus(new PartTag("nested")))
			);
			for (final FrontConstantPart fix : getSuffix())
				group.add(context, fix.createVisual(context, tags.plus(new PartTag("suffix"))));
			super.add(context, group, index);
		});
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
			if (retagLast)
				Helper.last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
		}
	}

	protected abstract Map<String, com.zarbosoft.luxemj.grammar.Node> getHotkeys();

	protected abstract List<FrontConstantPart> getPrefix();

	protected abstract List<FrontConstantPart> getSeparator();

	protected abstract List<FrontConstantPart> getSuffix();
}
