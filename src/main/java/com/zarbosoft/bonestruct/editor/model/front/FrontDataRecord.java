package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.Hotkeys;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.model.middle.DataRecord;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.nodes.*;
import com.zarbosoft.pidgoon.internal.Helper;
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
		private final DataRecord.Listener dataListener;
		private final DataRecord.Value data;
		private BorderAttachment borderAttachment;

		public RecordVisual(
				final Context context, final DataRecord.Value data, final Set<Tag> tags
		) {
			super(tags);
			this.data = data;
			// TODO replace dataListener with something that takes Context
			dataListener = new DataRecord.Listener() {

				@Override
				public void added(final Context context, final String key, final DataNode.Value value) {
					add(context, key, value);
				}

				@Override
				public void removed(final Context context, final String key) {
					remove(context, key);
				}
			};
			data.addListener(dataListener);
			data.get().forEach((k, v) -> add(context, k, v));
		}

		private class SeparatorGroup extends GroupVisualNode {

			public String key;

			public SeparatorGroup(final Context context) {
				super(ImmutableSet.of());
				final PSet<Tag> tags = HashTreePSet.from(RecordVisual.this.tags());
				for (final FrontConstantPart fix : separator)
					add(context, fix.createVisual(context, tags.plus(new PartTag("separator"))));
			}
		}

		private class ElementGroup extends GroupVisualNode {

			public String key;
			public DataPrimitive.Value keyLive;

			public ElementGroup(final Context context, final String key, final DataNode.Value value) {
				super(ImmutableSet.of());
				this.key = key;
				keyLive = new DataPrimitive.Value(key);
				final PSet<Tag> tags = HashTreePSet.from(RecordVisual.this.tags());
				for (final FrontConstantPart fix : prefix)
					add(context, fix.createVisual(context, tags.plus(new PartTag("prefix"))));
				final PrimitiveVisualNode keyNode =
						new PrimitiveVisualNode(context, keyLive, tags.plus(new PartTag("key"))) {
							@Override
							protected Iterable<Context.Action> getActions(final Context context) {
								return Iterables.concat(super.getActions(context),
										ImmutableList.of(new Context.Action() {

											@Override
											public void run(final Context context) {

											}

											@Override
											public String getName() {
												return "next";
											}
										})
								);
							}

							@Override
							protected void commit(final Context context) {
								if (!keyLive.get().equals(key)) {
									try {
										context.history.apply(context,
												new DataRecord.ChangeReplace(data, key, keyLive.get())
										);
									} catch (final UnknownError e) {
										// TODO figure out error
										new DataPrimitive.ChangeSet(keyLive, key).apply(context);
									}
								}
							}
						};
				add(context, keyNode);
				for (final FrontConstantPart fix : infix)
					add(context, fix.createVisual(context, tags.plus(new PartTag("infix"))));
				add(context, new NestedVisualNodePart(context, value, tags.plus(new PartTag("nested"))) {
					@Override
					protected Iterable<Context.Action> getActions(final Context context) {
						return Iterables.concat(super.getActions(context), ImmutableList.of(new Context.Action() {

							@Override
							public void run(final Context context) {

							}

							@Override
							public String getName() {
								return "previous";
							}
						}));
					}
				});
				for (final FrontConstantPart fix : suffix)
					add(context, fix.createVisual(context, tags.plus(new PartTag("suffix"))));
			}
		}

		@Override
		public boolean select(final Context context) {
			if (children.isEmpty())
				return false;
			((ElementParent) children.get(0).parent()).selectDown(context);
			return true;
		}

		@Override
		protected VisualNodeParent createParent(final int index) {
			if (children.get(index) instanceof SeparatorGroup)
				return new SeparatorParent(index);
			return new ElementParent(index, ((ElementGroup) children.get(index)).key);
		}

		public void remove(final Context context, final String key) {
			ElementParent parent = null;
			for (final VisualNodePart child : children) {
				// TODO children are sorted so this could be a binary search
				final ElementParent parent2;
				try {
					parent2 = (ElementParent) child.parent();
				} catch (final ClassCastException e) {
					continue;
				}
				if (!parent2.key.equals(key))
					continue;
				parent = parent2;
				break;
			}
			final int size = separator.isEmpty() || parent.getIndex() == children.size() - 1 ? 1 : 2;
			final boolean retagFirst = tagFirst && parent.getIndex() == 0;
			final boolean retagLast = tagLast && parent.getIndex() + size == children.size();
			remove(context, parent.getIndex(), size);
			if (!children.isEmpty()) {
				if (retagFirst)
					children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
				if (retagLast)
					Helper.last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
			}
		}

		private void add(final Context context, final String key, final DataNode.Value value) {
			int index = children.size();
			for (final VisualNodePart child : children) {
				// TODO children are sorted so this could be a binary search
				final ElementParent parent;
				try {
					parent = (ElementParent) child.parent();
				} catch (final ClassCastException e) {
					continue;
				}
				if (parent.key.compareTo(key) < 0)
					continue;
				index = parent.getIndex();
				break;
			}
			final boolean retagFirst = tagFirst && index == 0;
			final boolean retagLast = tagLast && index == children.size();
			if (!children.isEmpty()) {
				if (retagFirst)
					children.get(0).changeTags(context, new TagsChange().remove(new PartTag("first")));
				if (retagLast)
					Helper.last(children).changeTags(context, new TagsChange().remove(new PartTag("last")));
			}
			final PSet<Tag> tags = HashTreePSet.from(tags());
			if (index > 0 && !separator.isEmpty()) {
				index = index * 2 - 1;
				super.add(context, new SeparatorGroup(context), index++);
			}
			super.add(context, new ElementGroup(context, key, value), index);
			if (!children.isEmpty()) {
				if (retagFirst)
					children.get(0).changeTags(context, new TagsChange().add(new PartTag("first")));
				if (retagLast)
					Helper.last(children).changeTags(context, new TagsChange().add(new PartTag("last")));
			}
		}

		private class SeparatorParent extends GroupVisualNodeParent {

			public SeparatorParent(final int index) {
				super(RecordVisual.this, index);
			}

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				if (parent != null)
					return parent.hover(context, point);
				return null;
			}
		}

		private class ElementParent extends GroupVisualNodeParent {
			final String key;
			BorderAttachment border;
			boolean selected;

			public ElementParent(final int index, final String key) {
				super(RecordVisual.this, index);
				this.key = key;
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
						children.get(getIndex()).getFirstBrick(context),
						children.get(getIndex()).getLastBrick(context)
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
				if (selected)
					return null;
				if (hoverable != null)
					return hoverable;
				border = new BorderAttachment(context,
						context.syntax.hoverStyle,
						children.get(getIndex()).getFirstBrick(context),
						children.get(getIndex()).getLastBrick(context)
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
					border.setLast(context, children.get(getIndex()).getLastBrick(context));
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
