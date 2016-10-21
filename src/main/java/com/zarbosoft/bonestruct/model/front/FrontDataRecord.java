package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataRecord;
import com.zarbosoft.bonestruct.visual.Context;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "record")
public class FrontDataRecord implements FrontPart {

	@Luxem.Configuration
	public String middle;
	@Luxem.Configuration
	public List<FrontConstantPart> prefix;

	@Luxem.Configuration
	public static class KeyStyle extends FrontMark.Style {
		@Luxem.Configuration(name = "soft-indent", optional = true)
		public int softIndent = 0;
	}

	@Luxem.Configuration(name = "key-style", optional = true)
	public KeyStyle keyStyle = new KeyStyle();
	@Luxem.Configuration
	public List<FrontConstantPart> infix;
	@Luxem.Configuration(name = "value-style", optional = true)
	public FrontMark.Style valueStyle = new FrontMark.Style();
	@Luxem.Configuration
	public List<FrontConstantPart> suffix;
	@Luxem.Configuration
	public List<FrontConstantPart> separator;
	private DataRecord dataType;

	private class RecordVisual extends GroupVisualNode {
		private final ListChangeListener<Pair<StringProperty, Node>> dataListener;

		public RecordVisual(final Context context, final ObservableList<Pair<StringProperty, Node>> nodes) {
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
			super.remove(context, start, size);
			if (start == 0 && !children.isEmpty() && !separator.isEmpty())
				((GroupVisualNode) children.get(0)).remove(context, 0, 1);
		}

		private void add(final Context context, final int start, final List<Pair<StringProperty, Node>> nodes) {
			Helper.enumerate(nodes.stream(), start).forEach(p -> {
				final GroupVisualNode group = new GroupVisualNode() {
					@Override
					public Break breakMode() {
						return Break.NEVER;
					}

					@Override
					public String alignmentName() {
						return null;
					}

					@Override
					public String alignmentNameCompact() {
						return null;
					}
				};
				if (p.first > 0 && !separator.isEmpty()) {
					final GroupVisualNode separatorGroup = new GroupVisualNode() {
						@Override
						public Break breakMode() {
							return Break.NEVER;
						}

						@Override
						public String alignmentName() {
							return null;
						}

						@Override
						public String alignmentNameCompact() {
							return null;
						}
					};
					for (final FrontConstantPart fix : separator)
						separatorGroup.add(context, fix.createVisual(context));
					group.add(context, separatorGroup);
				}
				for (final FrontConstantPart fix : prefix)
					group.add(context, fix.createVisual(context));
				group.add(context, new PrimitiveVisualNode(context, p.second.first) {

					@Override
					protected int softIndent() {
						return keyStyle.softIndent;
					}

					@Override
					protected boolean breakFirst() {
						return false;
					}

					@Override
					protected String alignment() {
						return keyStyle.alignment;
					}

					@Override
					protected boolean level() {
						return false;
					}
				});
				for (final FrontConstantPart fix : infix)
					group.add(context, fix.createVisual(context));
				group.add(context, new NestedVisualNodePart(p.second.second.createVisual(context)) {
					@Override
					public Break breakMode() {
						return valueStyle.breakMode;
					}

					@Override
					public String alignmentName() {
						return valueStyle.alignment;
					}

					@Override
					public String alignmentNameCompact() {
						return valueStyle.alignmentCompact;
					}
				});
				for (final FrontConstantPart fix : suffix)
					group.add(context, fix.createVisual(context));
				super.add(context, group, p.first);
			});
		}

		@Override
		public Break breakMode() {
			return Break.NEVER;
		}

		@Override
		public String alignmentName() {
			return null;
		}

		@Override
		public String alignmentNameCompact() {
			return null;
		}
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Map<String, Object> data) {
		return new RecordVisual(context, dataType.get(data));
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataRecord(middle);
	}
}
