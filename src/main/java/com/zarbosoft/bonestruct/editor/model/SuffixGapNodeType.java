package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.back.*;
import com.zarbosoft.bonestruct.editor.model.front.*;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class SuffixGapNodeType extends NodeType {
	private final DataNode dataValue;
	private final DataPrimitive dataGap;
	@Configuration(name = "prefix", optional = true)
	public List<FrontConstantPart> frontPrefix = new ArrayList<>();
	@Configuration(name = "infix", optional = true)
	public List<FrontConstantPart> frontInfix = new ArrayList<>();
	@Configuration(name = "suffix", optional = true)
	public List<FrontConstantPart> frontSuffix = new ArrayList<>();

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, DataElement> middle;

	public SuffixGapNodeType() {
		id = "__suffix-gap";
		{
			final FrontDataNode value = new FrontDataNode();
			value.middle = "value";
			final FrontGapBase gap = new FrontGapBase() {

				@Override
				protected void buildChoices(
						final Context context, final Node self, final Map<String, List<Choice>> types
				) {
					for (final FreeNodeType type : context.syntax.types) {
						final Node.Parent replacementPoint = findReplacementPoint(context, self.parent, type);
						if (replacementPoint == null)
							continue;
						for (final FreeNodeType.GapKey key : type.gapKeys()) {
							if (key.indexBefore == -1)
								continue;
							if (!key.nodeBefore)
								continue;
							types.putIfAbsent(key.key, new ArrayList<>());
							types.get(key.key).add(new Choice(type, key.indexBefore, key.indexAfter));
						}
					}
				}

				@Override
				protected void choose(
						final Context context, final Node self, final Choice choice, final String remainder
				) {
					final SuffixGapNode suffixSelf = (SuffixGapNode) self;
					final Node node = choice.type.create();
					DataPrimitive.Value selectNext = findSelectNext(node, true);
					final com.zarbosoft.bonestruct.editor.model.Node replacement;
					if (selectNext == null) {
						replacement = context.syntax.suffixGap.create(true, node);
						selectNext = findSelectNext(replacement, false);
					} else {
						replacement = node;
					}
					final DataElement.Value value = suffixSelf.data.get("value");
					Node.Parent parent = suffixSelf.parent;
					if (suffixSelf.raise)
						parent = findReplacementPoint(context, parent, (FreeNodeType) node.type);
					parent.replace(context, node);
					node.type.front().get(choice.node).dispatch(new NodeOnlyDispatchHandler() {
						@Override
						public void handle(final FrontDataArrayBase front) {
							context.history.apply(context,
									new DataArrayBase.ChangeAdd((DataArrayBase.Value) node.data.get(front.middle()),
											0,
											ImmutableList.of(node)
									)
							);
						}

						@Override
						public void handle(final FrontDataNode front) {
							context.history.apply(context,
									new DataNode.ChangeSet((DataNode.Value) node.data.get(front.middle), node)
							);
						}
					});
					select(context, selectNext);
					setRemainder(context, selectNext, remainder);
				}

				private Node.Parent findReplacementPoint(
						final Context context, final Node.Parent start, final FreeNodeType type
				) {
					Node.Parent parent = null;
					Node.Parent test = start;
					//Node testNode = test.data().parent().node();
					Node testNode;
					while (test != null) {
						boolean allowed = false;

						if (context.syntax.getLeafTypes(test.childType()).contains(type.id)) {
							parent = test;
							allowed = true;
						}

						if (test.data().parent() == null)
							break;
						testNode = test.data().parent().node();

						// Can't move up if current level is bounded by any other front parts
						final int index = getIndexOfData(test, testNode);
						final List<FrontPart> front = testNode.type.front();
						if (index != front.size() - 1)
							break;
						final FrontPart frontNext = front.get(index);
						if (frontNext instanceof FrontDataArray && !((FrontDataArray) frontNext).suffix.isEmpty())
							break;

						if (allowed) {
							// Can't move up if next level has lower precedence
							if (testNode.type.precedence() < type.precedence)
								break;

							// Can't move up if next level has same precedence and parent is forward-associative
							if (testNode.type.precedence() == type.precedence && testNode.type.frontAssociative())
								break;
						}

						test = testNode.parent;
					}
					return parent;
				}

				private int getIndexOfData(final Node.Parent parent, final Node node) {
					return Common.enumerate(node.type.front().stream()).filter(pair -> {
						FrontPart front = pair.second;
						String id = null;
						if (front instanceof FrontDataNode)
							id = ((FrontDataNode) front).middle;
						else if (front instanceof FrontDataArray)
							id = ((FrontDataArray) front).middle;
						return parent.id().equals(id);
					}).map(pair -> pair.first).findFirst().get();
				}

			};
			front = ImmutableList.copyOf(Iterables.concat(frontPrefix,
					ImmutableList.of(gap),
					frontInfix,
					ImmutableList.of(value),
					frontSuffix
			));
		}
		{
			final BackType type = new BackType();
			type.value = "__gap";
			final BackDataNode value = new BackDataNode();
			value.middle = "value";
			final BackDataPrimitive gap = new BackDataPrimitive();
			gap.middle = "gap";
			final BackRecord record = new BackRecord();
			record.pairs.put("value", value);
			record.pairs.put("gap", gap);
			back = ImmutableList.of(type, record);
		}
		{
			dataValue = new DataNode();
			dataValue.id = "value";
			dataGap = new DataPrimitive();
			dataGap.id = "gap";
			middle = ImmutableMap.of("gap", dataGap, "value", dataValue);
		}
	}

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, DataElement> middle() {
		return middle;
	}

	@Override
	public List<BackPart> back() {
		return back;
	}

	@Override
	protected Map<String, AlignmentDefinition> alignments() {
		return ImmutableMap.of();
	}

	@Override
	public int precedence() {
		return 1_000_000;
	}

	@Override
	public boolean frontAssociative() {
		return false;
	}

	@Override
	public String name() {
		return "Gap (suffix)";
	}

	private class SuffixGapNode extends Node {
		private final boolean raise;

		public SuffixGapNode(
				final NodeType type, final Map<String, DataElement.Value> data, final boolean raise
		) {
			super(type, data);
			this.raise = raise;
		}
	}

	public Node create(final boolean raise, final Node value) {
		return new SuffixGapNode(this,
				ImmutableMap.of("value",
						new DataNode.Value(dataValue, value),
						"gap",
						new DataPrimitive.Value(dataGap, "")
				),
				raise
		);
	}
}
