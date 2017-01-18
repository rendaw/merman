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
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualBorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.nodes.GroupVisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.GroupVisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.nodes.NestedVisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.nodes.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
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
		private RecordSelection selection;
		private RecordHover hoverable;

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

			@Override
			public Brick createFirstBrick(final Context context) {
				final Brick out = super.createFirstBrick(context);
				if (selection != null && selection.key.equals(key)) {
					selection.border.setFirst(context, out);
					selection.border.notifySeedBrick(context, out);
				} else if (hoverable != null && hoverable.key.equals(key)) {
					hoverable.border.setFirst(context, out);
					hoverable.border.notifySeedBrick(context, out);
				}
				return out;
			}

			@Override
			public Brick createLastBrick(final Context context) {
				final Brick out = super.createLastBrick(context);
				if (selection != null && selection.key.equals(key)) {
					selection.border.setLast(context, out);
					selection.border.notifySeedBrick(context, out);
				} else if (hoverable != null && hoverable.key.equals(key)) {
					hoverable.border.setLast(context, out);
					hoverable.border.notifySeedBrick(context, out);
				}
				return out;
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
			final ElementParent parent = getParentByKey(key);
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
			if (selection != null && selection.key.equals(key)) {
				getNearestParentByKey(key).selectDown(context);
			} else if (hoverable != null && hoverable.key.equals(key))
				context.clearHover();
		}

		private void add(final Context context, final String key, final DataNode.Value value) {
			int index = getNearestIndexByKey(key);
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

			public ElementParent(final int index, final String key) {
				super(RecordVisual.this, index);
				this.key = key;
			}

			public void selectDown(final Context context) {
				if (selection != null && selection.key.equals(key))
					throw new AssertionError("Already selected");
				if (selection == null)
					selection = new RecordSelection(context);
				selection.setKey(context, key);
			}

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				if (selection != null && selection.key.equals(key))
					return null;
				if (hoverable == null)
					hoverable = new RecordHover(context);
				hoverable.setKey(context, key);
				return hoverable;
			}

			@Override
			public Brick createNextBrick(final Context context) {
				final Brick out = super.createNextBrick(context);
				if (selection != null && selection.key.equals(key))
					selection.border.notifyNextBrickPastEdge(context, out);
				else if (hoverable != null && hoverable.key.equals(key))
					hoverable.border.notifyNextBrickPastEdge(context, out);
				return out;
			}

			@Override
			public Brick createPreviousBrick(final Context context) {
				final Brick out = super.createPreviousBrick(context);
				if (selection != null && selection.key.equals(key))
					selection.border.notifyPreviousBrickPastEdge(context, out);
				else if (hoverable != null && hoverable.key.equals(key))
					hoverable.border.notifyPreviousBrickPastEdge(context, out);
				return out;
			}

			public ElementGroup elementTarget() {
				return (ElementGroup) target;
			}
		}

		private int getNearestIndexByKey(final String key) {
			final ElementParent out = getParentByKey(key);
			if (out == null)
				return children.size();
			return out.index;
		}

		private ElementGroup getChildByKey(final String key) {
			final ElementParent out = getParentByKey(key);
			if (out == null)
				return null;
			return out.elementTarget();
		}

		private ElementParent getNearestParentByKey(final String key) {
			for (final VisualNodePart child : children) {
				// TODO children are sorted so this could be a binary search
				final RecordVisual.ElementParent parent;
				try {
					parent = (RecordVisual.ElementParent) child.parent();
				} catch (final ClassCastException e) {
					continue;
				}
				if (parent.key.compareTo(key) < 0)
					continue;
				return parent;
			}
			return null;
		}

		private ElementParent getParentByKey(final String key) {
			final ElementParent out = getNearestParentByKey(key);
			if (!out.key.equals(key))
				return null;
			return out;
		}

		private class RecordHover extends Context.Hoverable {
			final private VisualBorderAttachment border;
			private String key;

			private RecordHover(final Context context) {
				border = new VisualBorderAttachment(context, context.syntax.hoverStyle);
			}

			@Override
			protected void clear(final Context context) {
				border.destroy(context);
				hoverable = null;
			}

			@Override
			public void click(final Context context) {
				((ElementParent) getChildByKey(key).parent).selectDown(context);
			}

			public void setKey(final Context context, final String key) {
				if (this.key == key)
					return;
				this.key = key;
				border.setFirst(context, getChildByKey(key));
			}
		}

		private class RecordSelection extends Context.Selection {
			String key;
			private final VisualBorderAttachment border;

			private RecordSelection(final Context context) {
				border = new VisualBorderAttachment(context, context.syntax.hoverStyle);
			}

			@Override
			protected Hotkeys getHotkeys(final Context context) {
				return context.getHotkeys(tags());
			}

			@Override
			protected void clear(final Context context) {
				border.destroy(context);
				selection = null;
			}

			public void setKey(final Context context, final String key) {
				if (this.key == key)
					return;
				this.key = key;
				border.setFirst(context, getChildByKey(key));
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

			@Override
			public VisualNodePart getVisual() {
				return RecordVisual.this;
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
