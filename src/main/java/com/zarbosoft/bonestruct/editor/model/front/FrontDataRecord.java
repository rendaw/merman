package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataRecord;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.*;
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
		private BorderAttachment borderAttachment;

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

		private class ChildGroup extends GroupVisualNode {

			private final boolean selectable;

			public ChildGroup(final Set<Tag> tags, final boolean selectable) {
				super(tags);
				this.selectable = selectable;
			}
		}

		@Override
		protected VisualNodeParent createParent(final int index) {
			final boolean selectable = ((ChildGroup) children.get(index)).selectable;
			return new GroupVisualNodeParent(this, index) {
				class RecordHoverable extends Context.Hoverable {

					BorderAttachment borderAttachment;

					RecordHoverable(final Context context, final int index) {
						borderAttachment = new BorderAttachment(context,
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

				RecordHoverable hoverable;

				@Override
				public Context.Hoverable hover(final Context context) {
					if (!selectable) {
						if (parent != null)
							return parent.hover(context);
						return null;
					}
					if (hoverable != null)
						return hoverable;
					hoverable = new RecordHoverable(context, index);
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
			if (!separator.isEmpty()) {
				size += size - 1;
				start *= 2;
			}
			final boolean retagFirst = tagFirst && start == 0;
			final boolean retagLast = tagLast && start + size == children.size();
			super.remove(context, start, size);
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
				int index = p.first;
				if (p.first > 0 && !separator.isEmpty()) {
					index = index * 2 - 1;
					final ChildGroup group = new ChildGroup(ImmutableSet.of(), false);
					for (final FrontConstantPart fix : separator)
						group.add(context, fix.createVisual(context, tags.plus(new PartTag("separator"))));
					super.add(context, group, index++);
				}
				final ChildGroup group = new ChildGroup(ImmutableSet.of(), true);
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
				super.add(context, group, index);
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
