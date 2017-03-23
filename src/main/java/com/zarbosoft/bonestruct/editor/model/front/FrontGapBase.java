package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
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

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle());
		this.dataType = nodeType.getDataPrimitive(middle());
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

			private final DataPrimitive.Value dataGap;

			private final NavigableMap<String, List<Choice>> types = new TreeMap<>();

			public GapSelection(
					final Context context, final int beginOffset, final int endOffset, final boolean direct
			) {
				super(context, beginOffset, endOffset, direct);
				dataGap = dataType.get(data);
				final com.zarbosoft.pidgoon.nodes.Union union = new com.zarbosoft.pidgoon.nodes.Union();
				buildChoices(context, dataGap.parent.node(), types);
			}

			@Override
			public void receiveText(final Context context, final String text) {
				super.receiveText(context, text);
				process(context);
			}

			private void process(final Context context) {
				final String string = dataGap.get();
				if (string.isEmpty())
					return;
				System.out.format("str %s\n", string);
				for (final Map.Entry<String, List<Choice>> pair : types.entrySet()) {
					System.out.format("pair %s\n", pair.getKey());
					if (string.length() > pair.getKey().length()) {
						if (string.startsWith(pair.getKey())) {
							for (final Choice choice : pair.getValue()) {
								System.out.format("choice %s %s\n", pair.getKey(), choice.type.id);
								if (choice.type.immediateMatch) {
									choose(context, choice, string.substring(pair.getKey().length()));
									return;
								}
								// TODO set details
							}
						}
					} else if (pair.getKey().startsWith(string)) {
						for (final Choice choice : pair.getValue()) {
							System.out.format("choice %s %s\n", pair.getKey(), choice.type.id);
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
		if (remainder == null || remainder.isEmpty())
			return;
		context.history.apply(context, new DataPrimitive.ChangeAdd((DataPrimitive.Value) selectNext, 0, remainder));
	}

	public DataPrimitive.Value findSelectNext(
			final com.zarbosoft.bonestruct.editor.model.Node node, boolean skipFirstNode
	) {
		for (final FrontPart front : node.type.front()) {
			if (front instanceof FrontDataPrimitive) {
				return (DataPrimitive.Value) node.data.get(((FrontDataPrimitive) front).middle);
			} else if (front instanceof FrontGapBase) {
				return (DataPrimitive.Value) node.data.get(middle());
			} else if (front instanceof FrontDataNode) {
				if (skipFirstNode) {
					skipFirstNode = false;
				} else {
					final DataPrimitive.Value found =
							findSelectNext(((DataNode.Value) node.data.get(((FrontDataNode) front).middle)).get(),
									skipFirstNode
							);
					if (found != null)
						return found;
				}
			} else if (front instanceof FrontDataArray) {
				final DataArrayBase.Value array = (DataArrayBase.Value) node.data.get(((FrontDataArray) front).middle);
				for (final Node element : array.get()) {
					if (skipFirstNode) {
						skipFirstNode = false;
					} else {
						final DataPrimitive.Value found = findSelectNext(element, skipFirstNode);
						if (found != null)
							return found;
					}
				}
			}
		}
		return null;
	}

	protected void select(final Context context, final DataPrimitive.Value value) {
		value.parent.node().getVisual().frontToData.get(value.data.id).select(context);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public String middle() {
		return "gap";
	}
}
