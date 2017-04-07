package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.back.BackDataPrimitive;
import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.back.BackType;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.model.front.FrontGapBase;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
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
import com.zarbosoft.rendaw.common.Pair;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration
public class GapNodeType extends NodeType {
	private final DataPrimitive dataGap;
	@Configuration
	public List<FrontConstantPart> frontPrefix = new ArrayList<>();
	@Configuration
	public List<FrontConstantPart> frontSuffix = new ArrayList<>();

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, DataElement> middle;

	public GapNodeType() {
		id = "__gap";
		{
			final FrontGapBase gap = new FrontGapBase() {
				@Override
				protected void process(
						final Context context, final Node self, final String string, final Common.UserData store
				) {
					class Choice {
						private final FreeNodeType type;
						private final GapKey key;

						Choice(
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
							DataPrimitive.Value selectNext = findSelectNext(node, false);
							final com.zarbosoft.bonestruct.editor.model.Node replacement;
							if (selectNext == null) {
								replacement = context.syntax.suffixGap.create(true, node);
								selectNext = findSelectNext(replacement, false);
							} else {
								replacement = node;
							}
							self.parent.replace(context, replacement);
							select(context, selectNext);
							if (!remainder.isEmpty())
								context.selection.receiveText(context, remainder);
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
					final Iterable<Choice> choices = iterable(Stream.concat(
							longest.first.results.stream().map(result -> (Choice) result),
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

				@Override
				protected void deselect(
						final Context context, final Node self, final String string, final Common.UserData userData
				) {
					if (!string.isEmpty())
						return;
					if (self.parent == null)
						return;
					final DataElement.Value parentValue = self.parent.data();
					if (parentValue instanceof DataArrayBase.Value) {
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
			dataGap = new DataPrimitive();
			dataGap.id = "gap";
			middle = ImmutableMap.of("gap", dataGap);
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
		return "Gap";
	}

	public Node create() {
		return new Node(this, ImmutableMap.of("gap", new DataPrimitive.Value(dataGap, "")));
	}
}
