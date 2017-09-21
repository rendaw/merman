package com.zarbosoft.merman.syntax;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.*;
import com.zarbosoft.merman.syntax.front.*;
import com.zarbosoft.merman.syntax.middle.MiddleArray;
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
public class PrefixGapAtomType extends AtomType {
	private final MiddleArray dataValue;
	private final MiddlePrimitive dataGap;
	@Configuration(name = "prefix", optional = true)
	public List<FrontSymbol> frontPrefix = new ArrayList<>();
	@Configuration(name = "infix", optional = true)
	public List<FrontSymbol> frontInfix = new ArrayList<>();
	@Configuration(name = "suffix", optional = true)
	public List<FrontSymbol> frontSuffix = new ArrayList<>();

	private List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, MiddlePart> middle;

	public PrefixGapAtomType() {
		{
			final BackDataPrimitive gap = new BackDataPrimitive();
			gap.middle = "gap";
			final BackDataArray value = new BackDataArray();
			value.middle = "value";
			final BackRecord record = new BackRecord();
			record.pairs.put("gap", gap);
			record.pairs.put("value", value);
			final BackType type = new BackType();
			type.type = "__gap";
			type.value = record;
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
	public void finish(
			final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes
	) {
		{
			final FrontGapBase gap = new FrontGapBase() {
				@Override
				protected List<? extends Choice> process(
						final Context context, final Atom self, final String string, final Common.UserData store
				) {
					final Atom value = ((ValueArray) self.data.get("value")).data.get(0);
					class PrefixChoice extends Choice {
						private final FreeAtomType type;
						private final GapKey key;

						PrefixChoice(final FreeAtomType type, final GapKey key) {
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

							// Place the atom
							self.parent.replace(context, atom);

							// Wrap the value in a prefix gap and place
							final Atom inner =
									parsed.nextInput == null ? context.syntax.prefixGap.create(value) : value;
							type.front().get(key.indexAfter).dispatch(new NodeOnlyDispatchHandler() {
								@Override
								public void handle(final FrontDataArrayBase front) {
									context.history.apply(context,
											new ChangeArray((ValueArray) atom.data.get(front.middle()),
													0,
													0,
													ImmutableList.of(inner)
											)
									);
								}

								@Override
								public void handle(final FrontDataAtom front) {
									context.history.apply(context,
											new ChangeNodeSet((ValueAtom) atom.data.get(front.middle), inner)
									);
								}
							});

							// Select the next input after the key
							if (parsed.nextInput != null)
								atom.data.get(parsed.nextInput.middle()).selectDown(context);
							else
								inner.visual.selectDown(context);
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
							return type == ((PrefixChoice) obj).type;
						}
					}

					// Get or build gap grammar
					final Grammar grammar = store.get(() -> {
						final Union union = new Union();
						for (final FreeAtomType type : (
								iterable(context.syntax.getLeafTypes(self.parent.childType()))
						)) {
							for (final GapKey key : gapKeys(syntax, type, value.type)) {
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
								).distinct().collect(Collectors.toList());
						for (final PrefixChoice choice : choices) {
							if (choices.size() <= choice.ambiguity()) {
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
						final Context context, final Atom self, final String string, final Common.UserData userData
				) {
					if (self.visual != null && string.isEmpty()) {
						self.parent.replace(context, ((ValueArray) self.data.get("value")).data.get(0));
					}
				}
			};
			final FrontDataArrayAsAtom value = new FrontDataArrayAsAtom();
			value.middle = "value";
			front = ImmutableList.copyOf(Iterables.concat(frontPrefix,
					ImmutableList.of(gap),
					frontInfix,
					ImmutableList.of(value),
					frontSuffix
			));
		}
		super.finish(syntax, allTypes, scalarTypes);
	}

	@Override
	public String id() {
		return "__prefix_gap";
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
		return "Gap (prefix)";
	}

	public Atom create(final Atom value) {
		return new Atom(this,
				ImmutableMap.of("value",
						new ValueArray(dataValue, ImmutableList.of(value)),
						"gap",
						new ValuePrimitive(dataGap, "")
				)
		);
	}

	public Atom create() {
		return new Atom(this,
				ImmutableMap.of("value",
						new ValueArray(dataValue, ImmutableList.of()),
						"gap",
						new ValuePrimitive(dataGap, "")
				)
		);
	}
}
