package com.zarbosoft.bonestruct.syntax.front;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.details.DetailsPage;
import com.zarbosoft.bonestruct.editor.display.Blank;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Group;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.display.derived.Box;
import com.zarbosoft.bonestruct.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.bonestruct.editor.display.derived.RowLayout;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePrimitive;
import com.zarbosoft.bonestruct.syntax.style.BoxStyle;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.syntax.symbol.SymbolText;
import com.zarbosoft.pidgoon.ParseContext;
import com.zarbosoft.pidgoon.bytes.Grammar;
import com.zarbosoft.pidgoon.bytes.Parse;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Wildcard;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.iterable;

public abstract class FrontGapBase extends FrontPart {
	private MiddlePrimitive dataType;

	@Override
	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new GapVisualPrimitive(context, parent, atom, tags);
	}

	@Override
	public void finish(final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle());
		this.dataType = atomType.getDataPrimitive(middle());
	}

	protected abstract List<? extends Choice> process(
			final Context context, final Atom self, final String string, final Common.UserData store
	);

	public abstract static class Choice {

		public abstract void choose(final Context context, final String string);

		public abstract String name();

		public abstract Iterable<? extends FrontPart> parts();
	}

	private class GapVisualPrimitive extends VisualPrimitive {
		private final Map<String, Value> data;

		public GapVisualPrimitive(
				final Context context, final VisualParent parent, final Atom atom, final PSet<Tag> tags
		) {
			super(context,
					parent,
					FrontGapBase.this.dataType.get(atom.data),
					tags
							.plus(new PartTag("gap"))
							.plusAll(FrontGapBase.this.tags
									.stream()
									.map(s -> new FreeTag(s))
									.collect(Collectors.toSet()))
			);
			this.data = atom.data;
		}

		@Override
		public PrimitiveSelection createSelection(
				final Context context, final boolean leadFirst, final int beginOffset, final int endOffset
		) {
			return new GapSelection(context, leadFirst, beginOffset, endOffset);
		}

		public class GapSelection extends PrimitiveSelection {

			private GapDetails gapDetails;

			private final ValuePrimitive self;
			private final Common.UserData userData = new Common.UserData();

			private class GapDetails extends DetailsPage {
				private final Box highlight;
				List<Pair<DisplayNode, DisplayNode>> rows = new ArrayList<>();
				private int index = 0;
				private int scroll = 0;
				private final Group tableGroup;
				private final Context.ContextIntListener edgeListener;

				public void updateScroll(final Context context) {
					final Pair<DisplayNode, DisplayNode> row = rows.get(index);
					final DisplayNode preview = row.first;
					final DisplayNode text = row.second;
					final int converse = preview.converse(context);
					final int converseEdge = text.converseEdge(context);
					scroll = Math.min(converse, Math.max(converseEdge - context.edge, scroll));
					tableGroup.setConverse(context, scroll, context.syntax.animateDetails);
				}

				private void changeChoice(final Context context, final int index) {
					this.index = index;
					final Pair<DisplayNode, DisplayNode> row = rows.get(index);
					final DisplayNode preview = row.first;
					final DisplayNode text = row.second;
					final int converse = preview.converse(context);
					final int transverse = Math.min(preview.transverse(context), text.transverse(context));
					final int converseEdge = text.converseEdge(context);
					final int transverseEdge = Math.max(preview.transverseEdge(context), text.transverseEdge(context));
					highlight.setSize(context, converse, transverse, converseEdge, transverseEdge);
					updateScroll(context);
				}

				public GapDetails(final Context context, final List<? extends Choice> choices) {
					this.edgeListener = new Context.ContextIntListener() {
						@Override
						public void changed(final Context context, final int oldValue, final int newValue) {
							updateScroll(context);
						}
					};
					context.addConverseEdgeListener(edgeListener);
					final Group group = context.display.group();
					this.node = group;

					final BoxStyle.Baked highlightStyle = new BoxStyle.Baked();
					highlightStyle.merge(context.syntax.gapChoiceStyle);
					highlight = Box.fromSettings(context, highlightStyle);
					group.add(highlight.drawing);

					final ColumnarTableLayout table = new ColumnarTableLayout(context, context.syntax.detailSpan);
					tableGroup = table.group;
					group.add(table.group);

					final PSet<Tag> tags = context.globalTags;
					final Style.Baked lineStyle =
							context.getStyle(tags.plus(new PartTag("details_choice")).plus(new PartTag("details")));
					final int transverse = 0;
					for (final Choice choice : choices) {
						final RowLayout previewLayout = new RowLayout(context.display);
						for (final FrontPart part : choice.parts()) {
							final DisplayNode node;
							if (part instanceof FrontSymbol) {
								node = ((FrontSymbol) part).createDisplay(context);
							} else if (part instanceof FrontDataPrimitive) {
								node = context.syntax.gapPlaceholder.createDisplay(context);
								context.syntax.gapPlaceholder.style(context, node, lineStyle);
							} else
								throw new DeadCode();
							previewLayout.add(node);
						}
						final Blank space = context.display.blank();
						space.setConverseSpan(context, 8);
						previewLayout.add(space);
						previewLayout.layout(context);

						final Text text = context.display.text();
						text.setColor(context, lineStyle.color);
						text.setFont(context, lineStyle.getFont(context));
						text.setText(context, choice.name());

						rows.add(new Pair<>(previewLayout.group, text));
						table.add(ImmutableList.of(previewLayout.group, text));
					}
					table.layout(context);
					changeChoice(context, 0);
					final List<Action> actions = new ArrayList<>();
					actions.addAll(ImmutableList.of(new Action() {
						@Override
						public void run(final Context context) {
							choices.get(index).choose(context, self.get());
						}

						@Override
						public String getName() {
							return "choose";
						}
					}, new Action() {
						@Override
						public void run(final Context context) {
							changeChoice(context, (index + 1) % choices.size());
						}

						@Override
						public String getName() {
							return "next_choice";
						}
					}, new Action() {
						@Override
						public void run(final Context context) {
							changeChoice(context, (index + choices.size() - 1) % choices.size());
						}

						@Override
						public String getName() {
							return "previous_choice";
						}
					}));
					for (int i = 0; i < 10; ++i) {
						final int i2 = i;
						actions.add(new Action() {
							@Override
							public void run(final Context context) {
								if (i2 < choices.size()) {
									choices.get(i2).choose(context, self.get());
								}
							}

							@Override
							public String getName() {
								return String.format("choose_%s", i2);
							}
						});
					}
					context.actions.put(this, actions);
				}

				public void destroy(final Context context) {
					context.actions.remove(this);
					context.removeConverseEdgeListener(edgeListener);
				}

				@Override
				public void tagsChanged(final Context context) {

				}
			}

			public GapSelection(
					final Context context, final boolean leadFirst, final int beginOffset, final int endOffset
			) {
				super(context, leadFirst, beginOffset, endOffset);
				self = dataType.get(data);
				if (self.data.length() > 0) {
					updateGap(context);
				}
			}

			public void updateGap(final Context context) {
				if (gapDetails != null) {
					context.details.removePage(context, gapDetails);
					gapDetails.destroy(context);
				}
				final List<? extends Choice> choices = process(context, self.parent.atom(), self.get(), userData);
				if (!choices.isEmpty()) {
					gapDetails = new GapDetails(context, choices);
					context.details.addPage(context, gapDetails);
				}
			}

			@Override
			public void receiveText(final Context context, final String text) {
				super.receiveText(context, text);
				updateGap(context);
			}

			@Override
			public void clear(final Context context) {
				super.clear(context);
				deselect(context, self.parent.atom(), self.get(), userData);
				if (gapDetails != null) {
					context.details.removePage(context, gapDetails);
					gapDetails.destroy(context);
					gapDetails = null;
				}
			}
		}
	}

	protected abstract void deselect(Context context, Atom self, String string, Common.UserData userData);

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public String middle() {
		return "gap";
	}

	protected static class GapKey {
		public int indexBefore;
		public List<FrontPart> keyParts = new ArrayList<>();
		public int indexAfter;

		public com.zarbosoft.pidgoon.Node matchGrammar(final FreeAtomType type) {
			final Sequence out = new Sequence();
			for (final FrontPart part : keyParts) {
				if (part instanceof FrontSymbol) {
					String text = ((FrontSymbol) part).gapKey;
					if (((FrontSymbol) part).type instanceof SymbolText)
						text = ((SymbolText) ((FrontSymbol) part).type).text;
					out.add(Grammar.stringSequence(text));
				} else if (part instanceof FrontDataPrimitive) {
					final MiddlePrimitive middle =
							(MiddlePrimitive) type.middle.get(((FrontDataPrimitive) part).middle);
					out.add(middle.validation == null ? new Repeat(new Wildcard()) : middle.validation.build());
				} else
					throw new DeadCode();
			}
			return out;
		}

		public class ParseResult {
			public Atom atom;
			public FrontPart nextInput;
			public String remainder;
		}

		public ParseResult parse(final Context context, final FreeAtomType type, final String string) {
			final ParseResult out = new ParseResult();
			final Iterator<FrontPart> frontIterator = keyParts.iterator();

			// Parse string into primitive parts
			final Set<String> filled = new HashSet<>();
			filled.addAll(type.middle.keySet());
			final Map<String, Value> data = new HashMap<>();
			int at = 0;
			for (final FrontPart front : iterable(frontIterator)) {
				final Grammar grammar = new Grammar();
				if (front instanceof FrontSymbol) {
					String text = ((FrontSymbol) front).gapKey;
					if (((FrontSymbol) front).type instanceof SymbolText)
						text = ((SymbolText) ((FrontSymbol) front).type).text;
					grammar.add("root", Grammar.stringSequence(text));
				} else if (front instanceof FrontDataPrimitive) {
					final MiddlePrimitive middle =
							(MiddlePrimitive) type.middle.get(((FrontDataPrimitive) front).middle);
					grammar.add("root",
							middle.validation == null ? new Repeat(new Wildcard()) : middle.validation.build()
					);
				} else
					throw new DeadCode();
				final Pair<ParseContext, Position> longest = new Parse<>()
						.grammar(grammar)
						.longestMatchFromStart(new ByteArrayInputStream(string
								.substring(at)
								.getBytes(StandardCharsets.UTF_8)));
				if (front instanceof FrontDataPrimitive) {
					data.put(front.middle(), new ValuePrimitive(type.getDataPrimitive(front.middle()),
							string.substring(at, at + (int) longest.second.distance())
					));
					filled.remove(front.middle());
				}
				at = at + (int) longest.second.distance();
				if (at >= string.length())
					break;
			}
			filled.forEach(middle -> data.put(middle, type.middle.get(middle).create(context.syntax)));
			out.remainder = string.substring(at);
			out.atom = new Atom(type, data);

			// Look for the next place to enter text
			for (final FrontPart part : iterable(frontIterator)) {
				if (part instanceof FrontDataPrimitive) {
					out.nextInput = part;
					break;
				}
			}

			return out;
		}
	}

	protected static List<GapKey> gapKeys(final FreeAtomType type) {
		final List<GapKey> out = new ArrayList<>();
		final Common.Mutable<GapKey> top = new Common.Mutable<>(new GapKey());
		top.value.indexBefore = -1;
		enumerate(type.front().stream()).forEach(p -> {
			p.second.dispatch(new FrontPart.DispatchHandler() {
				private void flush(final boolean node) {
					if (!top.value.keyParts.isEmpty()) {
						top.value.indexAfter = p.first;
						out.add(top.value);
						top.value = new GapKey();
					}
					top.value.indexBefore = p.first;
				}

				@Override
				public void handle(final FrontSymbol front) {
					if (front.condition != null && !front.condition.defaultOn())
						return;
					top.value.keyParts.add(front);
				}

				@Override
				public void handle(final FrontDataArrayBase front) {
					front.prefix.forEach(front2 -> front2.dispatch(this));
					flush(true);
					front.suffix.forEach(front2 -> front2.dispatch(this));
				}

				@Override
				public void handle(final FrontDataAtom front) {
					flush(true);
				}

				@Override
				public void handle(final FrontDataPrimitive front) {
					top.value.keyParts.add(front);
				}

				@Override
				public void handle(final FrontGapBase front) {
					throw new DeadCode();
				}
			});
		});
		if (!top.value.keyParts.isEmpty()) {
			top.value.indexAfter = -1;
			out.add(top.value);
		}
		return out;
	}
}
