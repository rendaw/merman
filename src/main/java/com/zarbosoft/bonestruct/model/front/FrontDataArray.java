package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataArray;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.parts.GroupVisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.NestedVisualNodePart;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Helper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "array")
public class FrontDataArray implements FrontPart {

	@Luxem.Configuration
	public String middle;
	@Luxem.Configuration
	public List<FrontConstantPart> prefix;
	@Luxem.Configuration(name = "value-style", optional = true)
	public FrontMark.Style valueStyle = new FrontMark.Style();
	@Luxem.Configuration
	public List<FrontConstantPart> suffix;
	@Luxem.Configuration
	public List<FrontConstantPart> separator;
	private DataArray dataType;

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataArray(middle);
	}

	private class ArrayVisualNode extends GroupVisualNode {

		private final ListChangeListener<Node> dataListener;

		public ArrayVisualNode(final Context context, final ObservableList<Node> nodes) {
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
			super.remove(context, start, size);
			if (start == 0 && !children.isEmpty() && !separator.isEmpty())
				((GroupVisualNode) children.get(0)).remove(context, 0, 1);
		}

		private void add(final Context context, final int start, final List<Node> nodes) {
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
				group.add(context, new NestedVisualNodePart(p.second.createVisual(context)) {
					@Override
					public Break breakMode() {
						return null;
					}

					@Override
					public String alignmentName() {
						return null;
					}

					@Override
					public String alignmentNameCompact() {
						return null;
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
		return new ArrayVisualNode(context, dataType.get(data));
	}
}
