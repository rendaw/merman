package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import org.pcollections.HashTreePSet;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FrontGapBase extends FrontPart {
	private DataPrimitive dataType;

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return new GapPrimitiveVisualNode(context, data, tags);
	}

	protected class Choice {
		public final FreeNodeType type;
		public final int node;
		public final int remainder;

		public Choice(final FreeNodeType type, final int node, final int remainder) {
			this.type = type;
			this.node = node;
			this.remainder = remainder;
		}
	}

	protected abstract void buildChoices(Context context, Node self, Map<String, List<Choice>> types);

	protected abstract void choose(
			final Context context, Node self, final Choice choice, final String remainder
	);

	private class GapPrimitiveVisualNode extends PrimitiveVisualNode {
		private final Map<String, DataElement.Value> data;

		public GapPrimitiveVisualNode(
				final Context context, final Map<String, DataElement.Value> data, final Set<Tag> tags
		) {
			super(context,
					FrontGapBase.this.dataType.get(data),
					HashTreePSet
							.from(tags)
							.plus(new PartTag("primitive"))
							.plusAll(FrontGapBase.this.tags
									.stream()
									.map(s -> new FreeTag(s))
									.collect(Collectors.toSet()))
			);
			this.data = data;
		}

		@Override
		public PrimitiveSelection createSelection(
				final Context context, final int beginOffset, final int endOffset, final boolean direct
		) {
			return new GapSelection(context, beginOffset, endOffset, direct);
		}

		public class GapSelection extends PrimitiveSelection {

			private final DataNode.Value dataValue;
			private final DataPrimitive.Value dataGap;
			private final DataPrimitive.Listener listener;

			private final NavigableMap<String, List<Choice>> types = new TreeMap<>();

			public GapSelection(
					final Context context, final int beginOffset, final int endOffset, final boolean direct
			) {
				super(context, beginOffset, endOffset, direct);
				dataValue = (DataNode.Value) data.get("value");
				dataGap = dataType.get(data);
				final DataPrimitive.Listener listener = new DataPrimitive.Listener() {
					@Override
					public void set(final Context context, final String value) {
						process(context);
					}

					@Override
					public void added(final Context context, final int index, final String value) {
						process(context);
					}

					@Override
					public void removed(final Context context, final int index, final int count) {
						process(context);
					}

				};
				dataGap.addListener(listener);
				this.listener = listener;
				final com.zarbosoft.pidgoon.nodes.Union union = new com.zarbosoft.pidgoon.nodes.Union();
				buildChoices(context, dataGap.parent.node(), types);
			}

			@Override
			public void clear(final Context context) {
				dataGap.removeListener(listener);
				super.clear(context);
			}

			private void process(final Context context) {
				final String string = dataGap.get();
				if (string.isEmpty())
					return;
				for (final Map.Entry<String, List<Choice>> pair : types.entrySet()) {
					if (string.length() > pair.getKey().length()) {
						if (string.startsWith(pair.getKey())) {
							for (final Choice choice : pair.getValue()) {
								if (choice.type.immediateMatch) {
									choose(context, choice, string.substring(pair.getKey().length()));
									return;
								}
								// TODO set details
							}
						}
					} else if (pair.getKey().startsWith(string)) {
						for (final Choice choice : pair.getValue()) {
							if (choice.type.immediateMatch) {
								choose(context, choice, null);
								return;
							}
							// TODO set details
						}
					}
				}
			}

			private void choose(
					final Context context, final Choice choice, final String remainder
			) {
				FrontGapBase.this.choose(context, dataGap.parent.node(), choice, remainder);
			}

		}
	}

	protected void setRemainder(
			final Context context, final DataElement.Value selectNext, final String remainder
	) {
		context.history.apply(context, new DataPrimitive.ChangeAdd((DataPrimitive.Value) selectNext, 0, remainder));
	}

	public class FindSelectNext {
		boolean skipped = false;

		public DataPrimitive.Value find(
				final com.zarbosoft.bonestruct.editor.model.Node node, final boolean skipFirstNode
		) {
			for (final FrontPart front : node.type.front()) {
				if (front instanceof FrontDataPrimitive) {
					return (DataPrimitive.Value) node.data.get(((FrontDataPrimitive) front).middle);
				} else if (front instanceof FrontDataNode) {
					if (skipFirstNode && !skipped) {
						skipped = true;
					} else {
						final DataPrimitive.Value found =
								find(((DataNode.Value) node.data.get(((FrontDataNode) front).middle)).get(),
										skipFirstNode
								);
						if (found != null)
							return found;
					}
				} else if (front instanceof FrontDataArray) {
					final DataArray.Value array = (DataArray.Value) node.data.get(((FrontDataArray) front).middle);
					for (final Node element : array.get()) {
						if (skipFirstNode && !skipped) {
							skipped = true;
						} else {
							final DataPrimitive.Value found = find(element, skipFirstNode);
							if (found != null)
								return found;
						}
					}
				}
			}
			return null;
		}
	}

	protected void select(final Context context, final DataPrimitive.Value value) {
		value.parent.node().getVisual().frontToData.get(value.data.id).select(context);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {

	}
}
