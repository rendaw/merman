package com.zarbosoft.bonestruct.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.BackDataPrimitive;
import com.zarbosoft.bonestruct.syntax.back.BackPart;
import com.zarbosoft.bonestruct.syntax.back.BackType;
import com.zarbosoft.bonestruct.syntax.front.*;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePart;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration
public class GapAtomType extends AtomType {
	private final MiddlePrimitive dataGap;
	@Configuration(name = "prefix")
	public List<FrontSymbol> frontPrefix = new ArrayList<>();
	@Configuration(name = "suffix")
	public List<FrontSymbol> frontSuffix = new ArrayList<>();

	private List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, MiddlePart> middle;

	public Value findSelectNext(
			final Context context, final Atom atom, boolean skipFirstNode
	) {
		if (atom.type == context.syntax.gap ||
				atom.type == context.syntax.prefixGap ||
				atom.type == context.syntax.suffixGap)
			return atom.data.get("gap");
		for (final FrontPart front : atom.type.front()) {
			if (front instanceof FrontDataPrimitive) {
				return atom.data.get(((FrontDataPrimitive) front).middle);
			} else if (front instanceof FrontGapBase) {
				return atom.data.get(middle());
			} else if (front instanceof FrontDataAtom) {
				if (skipFirstNode) {
					skipFirstNode = false;
				} else {
					final Value found = findSelectNext(context,
							((ValueAtom) atom.data.get(((FrontDataAtom) front).middle)).get(),
							skipFirstNode
					);
					if (found != null)
						return found;
				}
			} else if (front instanceof FrontDataArray) {
				final ValueArray array = (ValueArray) atom.data.get(((FrontDataArray) front).middle);
				if (array.data.isEmpty()) {
					if (skipFirstNode) {
						skipFirstNode = false;
					} else {
						final Value found =
								findSelectNext(context, array.createAndAddDefault(context, 0), skipFirstNode);
						if (found != null)
							return found;
						else
							return array;
					}
				} else
					for (final Atom element : array.data) {
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

	public GapAtomType() {
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
	public void finish(
			final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes
	) {
		{
			final FrontGapBase gap = new FrontGapBase() {
				@Override
				protected List<? extends Choice> process(
						final Context context, final Atom self, final String string, final Common.UserData store
				) {
					class GapChoice extends Choice {
						private final FreeAtomType type;
						private final GapKey key;

						GapChoice(
								final FreeAtomType type, final GapKey key
						) {
							this.type = type;
							this.key = key;
						}

						public int ambiguity() {
							return type.autoChooseAmbiguity;
						}

						public void choose(final Context context, final String string) {
							// Build atom
							final GapKey.ParseResult parsed = key.parse(context, type, string);
							final Atom atom = parsed.atom;
							final String remainder = parsed.remainder;

							// Place the atom
							Value selectNext = findSelectNext(context, atom, false);
							final Atom replacement;
							if (selectNext == null) {
								replacement = context.syntax.suffixGap.create(true, atom);
								selectNext = replacement.data.get("gap");
							} else {
								replacement = atom;
							}
							self.parent.replace(context, replacement);
							selectNext.selectDown(context);
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
						for (final FreeAtomType type : (
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
						final Context context, final Atom self, final String string, final Common.UserData userData
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
		super.finish(syntax, allTypes, scalarTypes);
	}

	@Override
	public String id() {
		return "__gap";
	}

	@Override
	public int depthScore() {
		return 0;
	}

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, MiddlePart> middle() {
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

	public Atom create() {
		return new Atom(this, ImmutableMap.of("gap", new ValuePrimitive(dataGap, "")));
	}
}