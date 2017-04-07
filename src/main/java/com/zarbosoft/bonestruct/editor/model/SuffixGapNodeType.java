package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.back.*;
import com.zarbosoft.bonestruct.editor.model.front.*;
import com.zarbosoft.bonestruct.editor.model.middle.*;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.ParseContext;
import com.zarbosoft.pidgoon.bytes.Grammar;
import com.zarbosoft.pidgoon.bytes.Operator;
import com.zarbosoft.pidgoon.bytes.Parse;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.nodes.Color;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration
public class SuffixGapNodeType extends NodeType {
	private final DataArray dataValue;
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
			final FrontDataArrayAsNode value = new FrontDataArrayAsNode();
			value.middle = "value";
			final FrontGapBase gap = new FrontGapBase() {
				private Pair<Node.Parent, Node> findReplacementPoint(
						final Context context, final Node.Parent start, final FreeNodeType type
				) {
					Node.Parent parent = null;
					Node child = null;
					Node.Parent test = start;
					//Node testNode = test.data().parent().node();
					Node testNode = null;
					while (test != null) {
						boolean allowed = false;

						if (context.syntax
								.getLeafTypes(test.childType())
								.map(t -> t.id)
								.collect(Collectors.toSet())
								.contains(type.id)) {
							parent = test;
							child = testNode;
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
							if (testNode.type.precedence() == type.precedence && !testNode.type.frontAssociative())
								break;
						}

						test = testNode.parent;
					}
					return new Pair<>(parent, child);
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

				@Override
				protected void process(
						final Context context, final Node self, final String string, final Common.UserData store
				) {
					class Choice {
						private final FreeNodeType type;
						private final GapKey key;

						public Choice(final FreeNodeType type, final GapKey key) {
							this.type = type;
							this.key = key;
						}

						public int ambiguity() {
							return type.autoChooseAmbiguity;
						}

						public void choose(final Context context, final String string) {
							final SuffixGapNode suffixSelf = (SuffixGapNode) self;

							Node root;
							Node.Parent rootPlacement;
							Node child;
							final DataElement.Value childPlacement;
							Node child2 = null;
							Node.Parent child2Placement = null;

							// Parse text into node as possible
							final GapKey.ParseResult parsed = key.parse(context, type, string);
							final Node node = parsed.node;
							final String remainder = parsed.remainder;
							root = node;
							child = ((DataArrayBase.Value) suffixSelf.data("value")).get().get(0);
							childPlacement = node.data(node.type.front().get(key.indexBefore).middle());
							final Node.Parent valuePlacementPoint2 = null;

							// Find the new node placement point
							rootPlacement = suffixSelf.parent;
							if (suffixSelf.raise) {
								final Pair<Node.Parent, Node> found =
										findReplacementPoint(context, rootPlacement, (FreeNodeType) parsed.node.type);
								if (found.first != rootPlacement) {
									// Raising new node up; the value will be placed at the original parent
									child2 = child;
									child = found.second;
									child2Placement = suffixSelf.parent;
									rootPlacement = found.first;
								}
							}

							// Find the selection/remainder entry point
							final DataPrimitive.Value selectNext;
							if (parsed.nextInput == null) {
								if (key.indexAfter == -1) {
									// No such place exists - wrap the placement node in a suffix gap
									root = context.syntax.suffixGap.create(true, node);
									selectNext = (DataPrimitive.Value) root.data("gap");
								} else {
									final Node gap = context.syntax.gap.create();
									final DataElement.Value nextNode =
											node.data(type.front.get(key.indexAfter).middle());
									if (nextNode instanceof DataNode.Value)
										context.history.apply(context,
												new DataNode.ChangeSet((DataNode.Value) nextNode, gap)
										);
									else if (nextNode instanceof DataArrayBase.Value)
										context.history.apply(context,
												new DataArrayBase.ChangeAdd((DataArrayBase.Value) nextNode,
														0,
														ImmutableList.of(gap)
												)
										);
									else
										throw new DeadCode();
									selectNext = (DataPrimitive.Value) gap.data("gap");
								}
							} else if (parsed.nextInput instanceof FrontDataPrimitive) {
								selectNext = (DataPrimitive.Value) node.data(parsed.nextInput.middle());
							} else if (parsed.nextInput instanceof FrontDataNode) {
								final DataNode.Value value1 = (DataNode.Value) node.data(parsed.nextInput.middle());
								final Node newGap = context.syntax.gap.create();
								context.history.apply(context, new DataNode.ChangeSet(value1, newGap));
								selectNext = (DataPrimitive.Value) newGap.data("gap");
							} else if (parsed.nextInput instanceof FrontDataArrayBase) {
								final DataArrayBase.Value value1 =
										(DataArrayBase.Value) node.data(parsed.nextInput.middle());
								final Node newGap = context.syntax.gap.create();
								context.history.apply(context,
										new DataArrayBase.ChangeAdd(value1, 0, ImmutableList.of(newGap))
								);
								selectNext = (DataPrimitive.Value) newGap.data("gap");
							} else
								throw new DeadCode();

							// Place everything starting from the bottom
							rootPlacement.replace(context, root);
							if (childPlacement instanceof DataNode.Value)
								context.history.apply(context,
										new DataNode.ChangeSet((DataNode.Value) childPlacement, child)
								);
							else if (childPlacement instanceof DataArrayBase.Value)
								context.history.apply(context,
										new DataArrayBase.ChangeAdd((DataArrayBase.Value) childPlacement,
												0,
												ImmutableList.of(child)
										)
								);
							else
								throw new DeadCode();
							if (child2Placement != null)
								child2Placement.replace(context, child2);

							// Select and dump remainder
							select(context, selectNext);
							if (!remainder.isEmpty())
								context.selection.receiveText(context, remainder);
						}
					}

					// Get or build gap grammar
					final Grammar grammar = store.get(() -> {
						final Union union = new Union();
						for (final FreeNodeType type : context.syntax.types) {
							final Pair<Node.Parent, Node> replacementPoint =
									findReplacementPoint(context, self.parent, type);
							if (replacementPoint.first == null)
								continue;
							for (final GapKey key : gapKeys(type)) {
								if (key.indexBefore == -1)
									continue;
								final Choice choice = new Choice(type, key);
								union.add(new Color(choice, new Operator(key.matchGrammar(type), store1 -> {
									return store1.pushStack(choice);
								})));
							}
						}
						final Grammar out = new Grammar();
						out.add("root", union);
						return out;
					});

					// If the whole text matches, try to auto complete
					// Display info on matches and not-yet-mismatches
					final Pair<ParseContext, Position> longest = new Parse<>()
							.grammar(grammar)
							.longestMatchFromStart(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
					final Iterable<Choice> choices =
							iterable(Stream.concat(longest.first.results.stream().map(result -> (Choice) result),
									longest.first.leaves.stream().map(leaf -> (Choice) leaf.color())
							));
					if (longest.second.distance() == string.length()) {
						for (final Choice choice : choices) {
							if (longest.first.leaves.size() <= choice.ambiguity()) {
								choice.choose(context, string);
								return;
							}
							// TODO add to details pane
						}
					} else if (longest.second.distance() >= 1) {
						for (final Choice choice : choices) {
							choice.choose(context, string);
							return;
						}
					}
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
			final BackDataArray value = new BackDataArray();
			value.middle = "value";
			final BackDataPrimitive gap = new BackDataPrimitive();
			gap.middle = "gap";
			final BackRecord record = new BackRecord();
			record.pairs.put("value", value);
			record.pairs.put("gap", gap);
			final BackType type = new BackType();
			type.value = "__gap";
			type.child = record;
			back = ImmutableList.of(type);
		}
		{
			dataValue = new DataArray();
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
		return new SuffixGapNode(this, ImmutableMap.of("value",
				new DataArray.Value(dataValue, ImmutableList.of(value)),
				"gap",
				new DataPrimitive.Value(dataGap, "")
		), raise);
	}
}
