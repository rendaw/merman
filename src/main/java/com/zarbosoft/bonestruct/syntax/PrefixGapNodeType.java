package com.zarbosoft.bonestruct.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeNodeSet;
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
public class PrefixGapNodeType extends NodeType {
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

	public PrefixGapNodeType() {
		id = "__prefix_gap";
		{
			final FrontGapBase gap = new FrontGapBase() {
				@Override
				protected List<? extends Choice> process(
						final Context context, final Node self, final String string, final Common.UserData store
				) {
					class PrefixChoice extends Choice {
						private final FreeNodeType type;
						private final GapKey key;

						PrefixChoice(final FreeNodeType type, final GapKey key) {
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

							// Place the node
							self.parent.replace(context, node);

							// Wrap the value in a prefix gap and place
							final Node value = ((ValueArray) self.data.get("value")).get().get(0);
							final Node inner =
									parsed.nextInput == null ? context.syntax.prefixGap.create(value) : value;
							type.front().get(key.indexAfter).dispatch(new NodeOnlyDispatchHandler() {
								@Override
								public void handle(final FrontDataArrayBase front) {
									context.history.apply(context,
											new ChangeArray((ValueArray) node.data.get(front.middle()),
													0,
													0,
													ImmutableList.of(inner)
											)
									);
								}

								@Override
								public void handle(final FrontDataNode front) {
									context.history.apply(context,
											new ChangeNodeSet((ValueNode) node.data.get(front.middle), inner)
									);
								}
							});

							// Select the next input after the key
							if (parsed.nextInput != null)
								node.data.get(parsed.nextInput.middle()).getVisual().selectDown(context);
							else
								inner.getVisual().selectDown(context);
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
							for (final GapKey key : gapKeys(type)) {
								if (key.indexAfter == -1)
									continue;
								final PrefixChoice choice = new PrefixChoice(type, key);
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
					if (longest.second.distance() == string.length()) {
						final List<PrefixChoice> choices =
								Stream.concat(longest.first.results.stream().map(result -> (PrefixChoice) result),
										longest.first.leaves.stream().map(leaf -> (PrefixChoice) leaf.color())
								).collect(Collectors.toList());
						for (final PrefixChoice choice : choices) {
							if (longest.first.leaves.size() <= choice.ambiguity()) {
								choice.choose(context, string);
								return ImmutableList.of();
							}
						}
						return choices;
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
			final FrontDataArrayAsNode value = new FrontDataArrayAsNode();
			value.middle = "value";
			front = ImmutableList.copyOf(Iterables.concat(frontPrefix,
					ImmutableList.of(gap),
					frontInfix,
					ImmutableList.of(value),
					frontSuffix
			));
		}
		{
			final BackDataPrimitive gap = new BackDataPrimitive();
			gap.middle = "gap";
			final BackDataArray value = new BackDataArray();
			value.middle = "value";
			final BackRecord record = new BackRecord();
			record.pairs.put("gap", gap);
			record.pairs.put("value", value);
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
		return "Gap (prefix)";
	}

	public Node create(final Node value) {
		return new Node(this,
				ImmutableMap.of("value",
						new ValueArray(dataValue, ImmutableList.of(value)),
						"gap",
						new ValuePrimitive(dataGap, "")
				)
		);
	}
}
