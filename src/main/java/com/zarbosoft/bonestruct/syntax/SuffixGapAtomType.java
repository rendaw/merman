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
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.*;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
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
public class SuffixGapAtomType extends AtomType {
	private final MiddleArray dataValue;
	private final MiddlePrimitive dataGap;
	@Configuration(name = "prefix", optional = true)
	public List<FrontSymbol> frontPrefix = new ArrayList<>();
	@Configuration(name = "infix", optional = true)
	public List<FrontSymbol> frontInfix = new ArrayList<>();
	@Configuration(name = "suffix", optional = true)
	public List<FrontSymbol> frontSuffix = new ArrayList<>();

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, MiddlePart> middle;

	/**
	 * @param type
	 * @param test
	 * @param allowed type is allowed to be placed here. Only for sliding suffix gaps.
	 * @return
	 */
	public static boolean isPrecedent(final FreeAtomType type, final Value.Parent test, final boolean allowed) {
		final Atom testAtom = test.value().parent.atom();

		// Can't move up if current level is bounded by any other front parts
		final int index = getIndexOfData(test, testAtom);
		final List<FrontPart> front = testAtom.type.front();
		if (index != front.size() - 1)
			return false;
		final FrontPart frontNext = front.get(index);
		if (frontNext instanceof FrontDataArray && !((FrontDataArray) frontNext).suffix.isEmpty())
			return false;

		if (allowed) {
			// Can't move up if next level has lower precedence
			if (testAtom.type.precedence() < type.precedence)
				return false;

			// Can't move up if next level has same precedence and parent is forward-associative
			if (testAtom.type.precedence() == type.precedence && !testAtom.type.frontAssociative())
				return false;
		}

		return true;
	}

	private static int getIndexOfData(final Value.Parent parent, final Atom atom) {
		return Common.enumerate(atom.type.front().stream()).filter(pair -> {
			FrontPart front = pair.second;
			String id = null;
			if (front instanceof FrontDataAtom)
				id = ((FrontDataAtom) front).middle;
			else if (front instanceof FrontDataArray)
				id = ((FrontDataArray) front).middle;
			return parent.id().equals(id);
		}).map(pair -> pair.first).findFirst().get();
	}

	public SuffixGapAtomType() {
		id = "__suffix_gap";
		{
			final FrontDataArrayAsAtom value = new FrontDataArrayAsAtom();
			value.middle = "value";
			final FrontGapBase gap = new FrontGapBase() {
				private Pair<Value.Parent, Atom> findReplacementPoint(
						final Context context, final Value.Parent start, final FreeAtomType type
				) {
					Value.Parent parent = null;
					Atom child = null;
					Value.Parent test = start;
					//Atom testAtom = test.value().parent().atom();
					Atom testAtom = null;
					while (test != null) {
						boolean allowed = false;

						if (context.syntax
								.getLeafTypes(test.childType())
								.map(t -> t.id)
								.collect(Collectors.toSet())
								.contains(type.id)) {
							parent = test;
							child = testAtom;
							allowed = true;
						}

						if (test.value().parent == null)
							break;
						testAtom = test.value().parent.atom();

						if (!isPrecedent(type, test, allowed))
							break;

						test = testAtom.parent;
					}
					return new Pair<>(parent, child);
				}

				@Override
				protected List<? extends Choice> process(
						final Context context, final Atom self, final String string, final Common.UserData store
				) {
					class SuffixChoice extends Choice {
						private final FreeAtomType type;
						private final GapKey key;

						public SuffixChoice(final FreeAtomType type, final GapKey key) {
							this.type = type;
							this.key = key;
						}

						public int ambiguity() {
							return type.autoChooseAmbiguity;
						}

						public void choose(final Context context, final String string) {
							final SuffixGapAtom suffixSelf = (SuffixGapAtom) self;

							Atom root;
							Value.Parent rootPlacement;
							Atom child;
							final Value childPlacement;
							Atom child2 = null;
							Value.Parent child2Placement = null;

							// Parse text into atom as possible
							final GapKey.ParseResult parsed = key.parse(context, type, string);
							final Atom atom = parsed.atom;
							final String remainder = parsed.remainder;
							root = atom;
							child = ((ValueArray) suffixSelf.data.get("value")).data.get(0);
							childPlacement = atom.data.get(atom.type.front().get(key.indexBefore).middle());
							final Value.Parent valuePlacementPoint2 = null;

							// Find the new atom placement point
							rootPlacement = suffixSelf.parent;
							if (suffixSelf.raise) {
								final Pair<Value.Parent, Atom> found =
										findReplacementPoint(context, rootPlacement, (FreeAtomType) parsed.atom.type);
								if (found.first != rootPlacement) {
									// Raising new atom up; the value will be placed at the original parent
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
									// No such place exists - wrap the placement atom in a suffix gap
									root = context.syntax.suffixGap.create(true, atom);
									selectNext = (ValuePrimitive) root.data.get("gap");
								} else {
									final Value nextNode = atom.data.get(type.front.get(key.indexAfter).middle());
									if (nextNode instanceof ValueAtom) {
										selectNext = (ValuePrimitive) ((ValueAtom) nextNode).data.data.get("gap");
									} else if (nextNode instanceof ValueArray) {
										final Atom gap = context.syntax.gap.create();
										context.history.apply(context,
												new ChangeArray((ValueArray) nextNode, 0, 0, ImmutableList.of(gap))
										);
										selectNext = (ValuePrimitive) gap.data.get("gap");
									} else
										throw new DeadCode();
								}
							} else if (parsed.nextInput instanceof FrontDataPrimitive) {
								selectNext = (ValuePrimitive) atom.data.get(parsed.nextInput.middle());
							} else if (parsed.nextInput instanceof FrontDataAtom) {
								final ValueAtom value1 = (ValueAtom) atom.data.get(parsed.nextInput.middle());
								final Atom newGap = context.syntax.gap.create();
								context.history.apply(context, new ChangeNodeSet(value1, newGap));
								selectNext = (ValuePrimitive) newGap.data.get("gap");
							} else if (parsed.nextInput instanceof FrontDataArrayBase) {
								final ValueArray value1 = (ValueArray) atom.data.get(parsed.nextInput.middle());
								final Atom newGap = context.syntax.gap.create();
								context.history.apply(context, new ChangeArray(value1, 0, 0, ImmutableList.of(newGap)));
								selectNext = (ValuePrimitive) newGap.data.get("gap");
							} else
								throw new DeadCode();

							// Place everything starting from the bottom
							rootPlacement.replace(context, root);
							if (childPlacement instanceof ValueAtom)
								context.history.apply(context, new ChangeNodeSet((ValueAtom) childPlacement, child));
							else if (childPlacement instanceof ValueArray)
								context.history.apply(context,
										new ChangeArray((ValueArray) childPlacement, 0, 0, ImmutableList.of(child))
								);
							else
								throw new DeadCode();
							if (child2Placement != null)
								child2Placement.replace(context, child2);

							// Select and dump remainder
							selectNext.visual().selectDown(context);
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
						for (final FreeAtomType type : context.syntax.types) {
							final Pair<Value.Parent, Atom> replacementPoint =
									findReplacementPoint(context, self.parent, type);
							if (replacementPoint.first == null)
								continue;
							for (final GapKey key : gapKeys(type)) {
								if (key.indexBefore == -1)
									continue;
								final Choice choice = new SuffixChoice(type, key);
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
					final List<SuffixChoice> choices =
							Stream.concat(longest.first.results.stream().map(result -> (SuffixChoice) result),
									longest.first.leaves.stream().map(leaf -> (SuffixChoice) leaf.color())
							).collect(Collectors.toList());
					if (longest.second.distance() == string.length()) {
						for (final SuffixChoice choice : choices) {
							if (longest.first.leaves.size() <= choice.ambiguity()) {
								choice.choose(context, string);
								return ImmutableList.of();
							}
						}
						return choices;
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
						final Context context, final Atom self, final String string, final Common.UserData userData
				) {
					if (self.visual() != null && string.isEmpty()) {
						self.parent.replace(context, ((ValueArray) self.data.get("value")).data.get(0));
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
		return "Gap (suffix)";
	}

	private class SuffixGapAtom extends Atom {
		private final boolean raise;

		public SuffixGapAtom(
				final AtomType type, final Map<String, Value> data, final boolean raise
		) {
			super(type, data);
			this.raise = raise;
		}
	}

	public Atom create(final boolean raise, final Atom value) {
		return new SuffixGapAtom(this,
				ImmutableMap.of("value",
						new ValueArray(dataValue, ImmutableList.of(value)),
						"gap",
						new ValuePrimitive(dataGap, "")
				),
				raise
		);
	}

	public Atom create(final boolean raise) {
		return new SuffixGapAtom(this,
				ImmutableMap.of("value",
						new ValueArray(dataValue, ImmutableList.of()),
						"gap",
						new ValuePrimitive(dataGap, "")
				),
				raise
		);
	}

}
