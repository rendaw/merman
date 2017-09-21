package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackDataPrimitive;
import com.zarbosoft.merman.syntax.back.BackPart;
import com.zarbosoft.merman.syntax.back.BackType;
import com.zarbosoft.merman.syntax.front.*;
import com.zarbosoft.merman.syntax.middle.MiddlePart;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitive;
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
			backType.type = "__gap";
			backType.value = backDataPrimitive;
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
							Atom root = atom;
							final Value.Parent rootPlacement = self.parent;

							// Find the selection/remainder entry point
							Value selectNext = null;
							FrontPart nextWhatever = null;
							if (parsed.nextInput == null) {
								if (key.indexAfter == -1) {
									// No such place exists - wrap the placement atom in a suffix gap
									root = context.syntax.suffixGap.create(true, atom);
									selectNext = (ValuePrimitive) root.data.get("gap");
								} else {
									nextWhatever = type.front.get(key.indexAfter);
								}
							} else
								nextWhatever = parsed.nextInput;
							if (selectNext == null) {
								if (nextWhatever instanceof FrontDataAtom) {
									selectNext = atom.data.get(nextWhatever.middle());
								} else if (nextWhatever instanceof FrontDataPrimitive ||
										nextWhatever instanceof FrontDataArrayBase) {
									selectNext = atom.data.get(nextWhatever.middle());
								} else
									throw new DeadCode();
							}

							// Place the atom
							rootPlacement.replace(context, root);

							// Select and dump remainder
							if (selectNext instanceof ValueAtom &&
									((ValueAtom) selectNext).data.visual.selectDown(context)) {
							} else
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

						@Override
						public boolean equals(final Object obj) {
							return type == ((GapChoice) obj).type;
						}
					}

					// Get or build gap grammar
					final Grammar grammar = store.get(() -> {
						final Union union = new Union();
						for (final FreeAtomType type : (
								iterable(context.syntax.getLeafTypes(self.parent.childType()))
						)) {
							for (final GapKey key : gapKeys(syntax, type, null)) {
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
							).distinct().collect(Collectors.toList());
					if (longest.second.distance() == string.length()) {
						for (final GapChoice choice : choices) {
							if (choices.size() <= choice.ambiguity()) {
								choice.choose(context, string);
								return ImmutableList.of();
							}
						}
					} else if (longest.second.distance() >= 1) {
						// When the text stops matching (new element started?) go ahead and choose a previous choice
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
