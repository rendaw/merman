package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataRecord;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.nodes.*;
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
		public boolean select(final Context context) {
			if (children.isEmpty())
				return false;
			((RecordVisualNodeParent) children.get(0).parent()).selectDown(context);
			return true;
		}

		@Override
		protected VisualNodeParent createParent(final int index) {
			final boolean selectable = ((ChildGroup) children.get(index)).selectable;
			return new RecordVisualNodeParent(index, selectable);
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
				group.add(context, new PrimitiveVisualNode(context, p.second.first, tags.plus(new PartTag("key"))) {
					@Override
					protected Iterable<Context.Action> getActions(final Context context) {
						return Iterables.concat(super.getActions(context), ImmutableList.of(new Context.Action() {

							@Override
							public void run(final Context context) {

							}

							@Override
							public String getName() {
								return "next";
							}
						}));
					}
				});
				for (final FrontConstantPart fix : infix)
					group.add(context, fix.createVisual(context, tags.plus(new PartTag("infix"))));
				group.add(context,
						new NestedVisualNodePart(p.second.second.createVisual(context),
								tags.plus(new PartTag("nested"))
						) {
							@Override
							protected Iterable<Context.Action> getActions(final Context context) {
								return Iterables.concat(super.getActions(context),
										ImmutableList.of(new Context.Action() {

											@Override
											public void run(final Context context) {

											}

											@Override
											public String getName() {
												return "previous";
											}
										})
								);
							}
						}
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

		private class RecordVisualNodeParent extends GroupVisualNodeParent {
			private final int index;
			private final boolean selectable;
			BorderAttachment border;
			boolean selected;

			public RecordVisualNodeParent(final int index, final boolean selectable) {
				super(RecordVisual.this, index);
				this.index = index;
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
				if (border != null)
					border.setLast(context, children.get(index).getLastBrick(context));
				return super.createNextBrick(context);
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
