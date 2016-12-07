package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.visual.Context;
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
	public void remove(final Context context, final int start, final int size) {
		final boolean retagFirst = tagFirst() && start == 0;
		final boolean retagLast = tagLast() && start + size == children.size();
		super.remove(context, start, size);
		if (start == 0 && !children.isEmpty() && !separator.isEmpty())
			((GroupVisualNode) children.get(0)).remove(context, 0, 1);
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
			if (retagLast)
				Helper.last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
		}
	}

	protected abstract boolean tagLast();

	protected abstract boolean tagFirst();

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
			final GroupVisualNode group = new GroupVisualNode(ImmutableSet.of());
			if (p.first > 0 && !separator.isEmpty()) {
				final GroupVisualNode separatorGroup = new GroupVisualNode(ImmutableSet.of());
				for (final FrontConstantPart fix : getSeparator())
					separatorGroup.add(context, fix.createVisual(context, tags.plus(new PartTag("separator"))));
				group.add(context, separatorGroup);
			}
			for (final FrontConstantPart fix : getPrefix())
				group.add(context, fix.createVisual(context, tags.plus(new PartTag("prefix"))));
			group.add(
					context,
					new NestedVisualNodePart(p.second.createVisual(context), tags.plus(new PartTag("nested")))
			);
			for (final FrontConstantPart fix : getSuffix())
				group.add(context, fix.createVisual(context, tags.plus(new PartTag("suffix"))));
			super.add(context, group, p.first);
		});
		if (!children.isEmpty()) {
			if (retagFirst)
				children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
			if (retagLast)
				Helper.last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
		}
	}

	protected abstract Map<String, com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node> getHotkeys();

	protected abstract List<FrontConstantPart> getPrefix();

	protected abstract List<FrontConstantPart> getSeparator();

	protected abstract List<FrontConstantPart> getSuffix();
}
