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
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.BackDataPrimitive;
import com.zarbosoft.bonestruct.syntax.back.BackPart;
import com.zarbosoft.bonestruct.syntax.back.BackType;
import com.zarbosoft.bonestruct.syntax.front.*;
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
public class GapNodeType extends NodeType {
	private final MiddlePrimitive dataGap;
	@Configuration
	public List<FrontConstantPart> frontPrefix = new ArrayList<>();
	@Configuration
	public List<FrontConstantPart> frontSuffix = new ArrayList<>();

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, MiddleElement> middle;

	public Value findSelectNext(
			final Context context, final Node node, boolean skipFirstNode
	) {
		for (final FrontPart front : node.type.front()) {
			if (front instanceof FrontDataPrimitive) {
				return node.data.get(((FrontDataPrimitive) front).middle);
			} else if (front instanceof FrontGapBase) {
				return node.data.get(middle());
			} else if (front instanceof FrontDataNode) {
				if (skipFirstNode) {
					skipFirstNode = false;
				} else {
					final Value found = findSelectNext(context,
							((ValueNode) node.data.get(((FrontDataNode) front).middle)).get(),
							skipFirstNode
					);
					if (found != null)
						return found;
				}
			} else if (front instanceof FrontDataArray) {
				final ValueArray array = (ValueArray) node.data.get(((FrontDataArray) front).middle);
				if (array.data.isEmpty()) {
					if (skipFirstNode) {
						skipFirstNode = false;
					} else {
						final Node newGap = context.syntax.gap.create();
						context.history.apply(context, new ChangeArray(array, 0, 0, ImmutableList.of(newGap)));
						return newGap.data.get("gap");
					}
				} else
					for (final Node element : array.get()) {
						if (skipFirstNode) {
							skipFirstNode = false;
						} else {
							final Value found = findSelectNext(context, element, skipFirstNode);
							if (found != null)
								return found;
						}
					}
			}
		}
		return null;
	}

	public GapNodeType() {
		id = "__gap";
		{
			final FrontGapBase gap = new FrontGapBase() {
				@Override
				protected List<? extends Choice> process(
						final Context context, final Node self, final String string, final Common.UserData store
				) {
					class GapChoice extends Choice {
						private final FreeNodeType type;
						private final GapKey key;

						GapChoice(
								final FreeNodeType type, final GapKey key
						) {
							this.type = type;
							this.key = key;
						}

						public int ambiguity() {
							return type.autoChooseAmbiguity;
						}

						public void choose(final Context context, final String string) {
							// Build node
							final GapKey.ParseResult parsed = key.parse(context, type, string);
							final Node node = parsed.node;
							final String remainder = parsed.remainder;

							// Place the node
							Value selectNext = findSelectNext(context, node, false);
							final Node replacement;
							if (selectNext == null) {
								replacement = context.syntax.suffixGap.create(true, node);
								selectNext = replacement.data.get("gap");
							} else {
								replacement = node;
							}
							self.parent.replace(context, replacement);
							selectNext.getVisual().selectDown(context);
							if (!remainder.isEmpty())
								context.selection.receiveText(context, remainder);
						}

						@Override
						public String name() {
							return type.name();
						}

						@Override
						public Iterable<? extends FrontPart> parts() {
							return key.keyParts;
						}
					}

					// Get or build gap grammar
					final Grammar grammar = store.get(() -> {
						final Union union = new Union();
						for (final FreeNodeType type : (
								self.parent == null ?
										iterable(context.syntax.getLeafTypes(context.syntax.root.type)) :
										iterable(context.syntax.getLeafTypes(self.parent.childType()))
						)) {
							final List<GapKey> gapKeys = gapKeys(type);
							for (final GapKey key : gapKeys(type)) {
								final GapChoice choice = new GapChoice(type, key);
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
					final List<GapChoice> choices =
							Stream.concat(longest.first.results.stream().map(result -> (GapChoice) result),
									longest.first.leaves.stream().map(leaf -> (GapChoice) leaf.color())
							).collect(Collectors.toList());
					if (longest.second.distance() == string.length()) {
						for (final GapChoice choice : choices) {
							if (longest.first.leaves.size() <= choice.ambiguity()) {
								choice.choose(context, string);
								return ImmutableList.of();
							}
						}
					} else if (longest.second.distance() >= 1) {
						for (final GapChoice choice : choices) {
							choice.choose(context, string);
							return ImmutableList.of();
						}
					}
					return choices;
				}

				@Override
				protected void deselect(
						final Context context, final Node self, final String string, final Common.UserData userData
				) {
					if (!string.isEmpty())
						return;
					if (self.parent == null)
						return;
					final Value parentValue = self.parent.value();
					if (parentValue instanceof ValueArray) {
						self.parent.delete(context);
					}
				}
			};
			front = ImmutableList.copyOf(Iterables.concat(frontPrefix, ImmutableList.of(gap), frontSuffix));
		}
		{
			final BackDataPrimitive backDataPrimitive = new BackDataPrimitive();
			backDataPrimitive.middle = "gap";
			final BackType backType = new BackType();
			backType.value = "__gap";
			backType.child = backDataPrimitive;
			back = ImmutableList.of(backType);
		}
		{
			dataGap = new MiddlePrimitive();
			dataGap.id = "gap";
			middle = ImmutableMap.of("gap", dataGap);
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
	public Map<String, AlignmentDefinition> alignments() {
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
		return "Gap";
	}

	public Node create() {
		return new Node(this, ImmutableMap.of("gap", new ValuePrimitive(dataGap, "")));
	}
}
