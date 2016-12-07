package com.zarbosoft.bonestruct.model.front;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataRecord;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.GroupVisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.NestedVisualNodePart;
import com.zarbosoft.bonestruct.visual.nodes.parts.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Luxem.Configuration(name = "record")
public class FrontDataRecord extends FrontPart {

	@Luxem.Configuration
	public String middle;
	@Luxem.Configuration
	public List<FrontConstantPart> prefix;
	@Luxem.Configuration
	public List<FrontConstantPart> infix;
	@Luxem.Configuration
	public List<FrontConstantPart> suffix;
	@Luxem.Configuration
	public List<FrontConstantPart> separator;
	private DataRecord dataType;
	@Luxem.Configuration(name = "tag-first", optional = true)
	public boolean tagFirst = false;
	@Luxem.Configuration(name = "tag-last", optional = true)
	public boolean tagLast = false;

	private class RecordVisual extends GroupVisualNode {
		private final ListChangeListener<Pair<StringProperty, Node>> dataListener;

		public RecordVisual(
				final Context context, final ObservableList<Pair<StringProperty, Node>> nodes, final Set<Tag> tags
		) {
			super(tags);
			// TODO replace dataListener with something that takes Context
			dataListener = c -> {
				while (c.next()) {
					if (c.wasPermutated()) {
						remove(context, c.getFrom(), c.getRemovedSize());
						add(context, c.getFrom(), nodes.subList(c.getFrom(), c.getTo()));
					} else if (c.wasUpdated()) {
						throw new AssertionError("Record data shouldn't be updated.");
					} else {
						remove(context, c.getFrom(), c.getRemovedSize());
						add(context, c.getFrom(), (List<Pair<StringProperty, Node>>) c.getAddedSubList());
					}
				}
			};
			nodes.addListener(new WeakListChangeListener<>(dataListener));
			add(context, 0, nodes);
		}

		@Override
		public void remove(final Context context, final int start, final int size) {
			final boolean retagFirst = tagFirst && start == 0;
			final boolean retagLast = tagLast && start + size == children.size();
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

		private void add(final Context context, final int start, final List<Pair<StringProperty, Node>> nodes) {
			final boolean retagFirst = tagFirst && start == 0;
			final boolean retagLast = tagLast && start == children.size();
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
					for (final FrontConstantPart fix : separator)
						separatorGroup.add(context, fix.createVisual(context, tags.plus(new PartTag("separator"))));
					group.add(context, separatorGroup);
				}
				for (final FrontConstantPart fix : prefix)
					group.add(context, fix.createVisual(context, tags.plus(new PartTag("prefix"))));
				group.add(context, new PrimitiveVisualNode(context, p.second.first, tags.plus(new PartTag("key"))));
				for (final FrontConstantPart fix : infix)
					group.add(context, fix.createVisual(context, tags.plus(new PartTag("infix"))));
				group.add(context,
						new NestedVisualNodePart(p.second.second.createVisual(context),
								tags.plus(new PartTag("nested"))
						)
				);
				for (final FrontConstantPart fix : suffix)
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
	}

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, Object> data, final Set<VisualNode.Tag> tags
	) {
		return new RecordVisual(context,
				dataType.get(data),
				HashTreePSet
						.from(tags)
						.plus(new VisualNode.PartTag("record"))
						.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
		);
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataRecord(middle);
	}
}
