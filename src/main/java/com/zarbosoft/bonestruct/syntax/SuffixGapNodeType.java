package com.zarbosoft.bonestruct.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.*;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePrimitive;
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

@Configuration
public class SuffixGapNodeType extends NodeType {
	private final MiddleArray dataValue;
	private final MiddlePrimitive dataGap;
	@Configuration(name = "prefix", optional = true)
	public List<FrontConstantPart> frontPrefix = new ArrayList<>();
	@Configuration(name = "infix", optional = true)
	public List<FrontConstantPart> frontInfix = new ArrayList<>();
	@Configuration(name = "suffix", optional = true)
	public List<FrontConstantPart> frontSuffix = new ArrayList<>();

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, MiddleElement> middle;

	public SuffixGapNodeType() {
		id = "__suffix_gap";
		{
			final FrontDataArrayAsNode value = new FrontDataArrayAsNode();
			value.middle = "value";
			final FrontGapBase gap = new FrontGapBase() {
				private Pair<Value.Parent, Node> findReplacementPoint(
						final Context context, final Value.Parent start, final FreeNodeType type
				) {
					Value.Parent parent = null;
					Node child = null;
					Value.Parent test = start;
					//Node testNode = test.value().parent().node();
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

						if (test.value().parent() == null)
							break;
						testNode = test.value().parent().node();

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

				private int getIndexOfData(final Value.Parent parent, final Node node) {
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
				protected List<String> process(
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
							Value.Parent rootPlacement;
							Node child;
							final Value childPlacement;
							Node child2 = null;
							Value.Parent child2Placement = null;

							// Parse text into node as possible
							final GapKey.ParseResult parsed = key.parse(context, type, string);
							final Node node = parsed.node;
							final String remainder = parsed.remainder;
							root = node;
							child = ((ValueArray) suffixSelf.data.get("value")).get().get(0);
							childPlacement = node.data.get(node.type.front().get(key.indexBefore).middle());
							final Value.Parent valuePlacementPoint2 = null;

							// Find the new node placement point
							rootPlacement = suffixSelf.parent;
							if (suffixSelf.raise) {
								final Pair<Value.Parent, Node> found =
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
							final ValuePrimitive selectNext;
							if (parsed.nextInput == null) {
								if (key.indexAfter == -1) {
									// No such place exists - wrap the placement node in a suffix gap
									root = context.syntax.suffixGap.create(true, node);
									selectNext = (ValuePrimitive) root.data.get("gap");
								} else {
									final Value nextNode = node.data.get(type.front.get(key.indexAfter).middle());
									if (nextNode instanceof ValueNode) {
										selectNext = (ValuePrimitive) ((ValueNode) nextNode).get().data.get("gap");
									} else if (nextNode instanceof ValueArray) {
										final Node gap = context.syntax.gap.create();
										context.history.apply(context,
												new ChangeArray((ValueArray) nextNode, 0, 0, ImmutableList.of(gap))
										);
										selectNext = (ValuePrimitive) gap.data.get("gap");
									} else
										throw new DeadCode();
								}
							} else if (parsed.nextInput instanceof FrontDataPrimitive) {
								selectNext = (ValuePrimitive) node.data.get(parsed.nextInput.middle());
							} else if (parsed.nextInput instanceof FrontDataNode) {
								final ValueNode value1 = (ValueNode) node.data.get(parsed.nextInput.middle());
								final Node newGap = context.syntax.gap.create();
								context.history.apply(context, new ChangeNodeSet(value1, newGap));
								selectNext = (ValuePrimitive) newGap.data.get("gap");
							} else if (parsed.nextInput instanceof FrontDataArrayBase) {
								final ValueArray value1 = (ValueArray) node.data.get(parsed.nextInput.middle());
								final Node newGap = context.syntax.gap.create();
								context.history.apply(context, new ChangeArray(value1, 0, 0, ImmutableList.of(newGap)));
								selectNext = (ValuePrimitive) newGap.data.get("gap");
							} else
								throw new DeadCode();

							// Place everything starting from the bottom
							rootPlacement.replace(context, root);
							if (childPlacement instanceof ValueNode)
								context.history.apply(context, new ChangeNodeSet((ValueNode) childPlacement, child));
							else if (childPlacement instanceof ValueArray)
								context.history.apply(context,
										new ChangeArray((ValueArray) childPlacement, 0, 0, ImmutableList.of(child))
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
							final Pair<Value.Parent, Node> replacementPoint =
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
					final List<Choice> choices =
							Stream.concat(longest.first.results.stream().map(result -> (Choice) result),
									longest.first.leaves.stream().map(leaf -> (Choice) leaf.color())
							).collect(Collectors.toList());
					if (longest.second.distance() == string.length()) {
						for (final Choice choice : choices) {
							if (longest.first.leaves.size() <= choice.ambiguity()) {
								choice.choose(context, string);
								return ImmutableList.of();
							}
						}
						return choices.stream().map(choice -> choice.type.id).collect(Collectors.toList());
					} else if (longest.second.distance() >= 1) {
						for (final Choice choice : choices) {
							choice.choose(context, string);
							return ImmutableList.of();
						}
					}
					return ImmutableList.of();
				}

				@Override
				protected void deselect(
						final Context context, final Node self, final String string, final Common.UserData userData
				) {
					if (self.getVisual() != null && string.isEmpty()) {
						self.parent.replace(context, ((ValueArray) self.data.get("value")).get().get(0));
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
			dataValue = new MiddleArray();
			dataValue.id = "value";
			dataGap = new MiddlePrimitive();
			dataGap.id = "gap";
			middle = ImmutableMap.of("gap", dataGap, "value", dataValue);
		}
	}

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, MiddleElement> middle() {
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
				final NodeType type, final Map<String, Value> data, final boolean raise
		) {
			super(type, data);
			this.raise = raise;
		}
	}

	public Node create(final boolean raise, final Node value) {
		return new SuffixGapNode(this,
				ImmutableMap.of("value",
						new ValueArray(dataValue, ImmutableList.of(value)),
						"gap",
						new ValuePrimitive(dataGap, "")
				),
				raise
		);
	}
}
