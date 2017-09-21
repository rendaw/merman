package com.zarbosoft.merman.syntax.front;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.Blank;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.display.derived.Box;
import com.zarbosoft.merman.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.merman.editor.display.derived.RowLayout;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.middle.MiddleArrayBase;
import com.zarbosoft.merman.syntax.middle.MiddleAtom;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitive;
import com.zarbosoft.merman.syntax.style.BoxStyle;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.SymbolText;
import com.zarbosoft.pidgoon.ParseContext;
import com.zarbosoft.pidgoon.bytes.Grammar;
import com.zarbosoft.pidgoon.bytes.Parse;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.nodes.Sequence;
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

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "gap";
		}
	}

	@Override
	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		return new GapVisualPrimitive(context, parent, atom, tags, visualDepth, depthScore);
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

		/**
		 * Lists front parts following (or preceding) the user provided data to preview what and from where
		 * will be completed.
		 *
		 * @return
		 */
		public abstract Iterable<? extends FrontPart> parts();
	}

	public class GapVisualPrimitive extends VisualPrimitive {
		private final Map<String, Value> data;

		public GapVisualPrimitive(
				final Context context,
				final VisualParent parent,
				final Atom atom,
				final PSet<Tag> tags,
				final int visualDepth,
				final int depthScore
		) {
			super(
					context,
					parent,
					FrontGapBase.this.dataType.get(atom.data),
					tags
							.plus(new PartTag("gap"))
							.plusAll(FrontGapBase.this.tags
									.stream()
									.map(s -> new FreeTag(s))
									.collect(Collectors.toSet())),
					visualDepth,
					depthScore
			);
			this.data = atom.data;
		}

		@Override
		public void select(final Context context, final boolean leadFirst, final int beginOffset, final int endOffset) {
			super.select(context, leadFirst, beginOffset, endOffset);
			if (((GapSelection) selection).self.data.length() > 0) {
				((GapSelection) selection).updateGap(context);
			}
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
					highlight.setSize(context, converseEdge - converse, transverseEdge - transverse);
					highlight.setPosition(context, new Vector(converse, transverse), false);
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

					final PSet<Tag> tags = context.globalTags;

					BoxStyle.Baked highlightStyle = context.getStyle(tags
							.plus(new PartTag("details_selection"))
							.plus(new PartTag("details"))).box;
					if (highlightStyle == null)
						highlightStyle = new BoxStyle.Baked();
					highlightStyle.merge(context.syntax.gapChoiceStyle);
					highlight = new Box(context);
					highlight.setStyle(context, highlightStyle);
					group.add(highlight.drawing);

					final ColumnarTableLayout table = new ColumnarTableLayout(context, context.syntax.detailSpan);
					tableGroup = table.group;
					group.add(table.group);

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
					actions.addAll(ImmutableList.of(
							new ActionChoose(choices),
							new ActionNextChoice(choices),
							new ActionPreviousChoice(choices)
					));
					for (int i = 0; i < 10; ++i) {
						final int i2 = i;
						actions.add(new ActionChooseIndex(i2, choices));
					}
					context.addActions(this, actions);
				}

				public void destroy(final Context context) {
					context.removeActions(this);
					context.removeConverseEdgeListener(edgeListener);
				}

				@Override
				public void tagsChanged(final Context context) {

				}

				@Action.StaticID(id = "choose")
				private class ActionChoose extends ActionBase {
					private final List<? extends Choice> choices;

					public ActionChoose(final List<? extends Choice> choices) {
						this.choices = choices;
					}

					@Override
					public boolean run(final Context context) {
						choices.get(index).choose(context, self.get());
						return true;
					}
				}

				@Action.StaticID(id = "next_choice")
				private class ActionNextChoice extends ActionBase {
					private final List<? extends Choice> choices;

					public ActionNextChoice(final List<? extends Choice> choices) {
						this.choices = choices;
					}

					@Override
					public boolean run(final Context context) {
						changeChoice(context, (index + 1) % choices.size());
						return true;
					}
				}

				@Action.StaticID(id = "previous_choice")
				private class ActionPreviousChoice extends ActionBase {
					private final List<? extends Choice> choices;

					public ActionPreviousChoice(final List<? extends Choice> choices) {
						this.choices = choices;
					}

					@Override
					public boolean run(final Context context) {
						changeChoice(context, (index + choices.size() - 1) % choices.size());
						return true;
					}
				}

				@Action.StaticID(id = "choose_%s (%s = index)")
				private class ActionChooseIndex extends ActionBase {
					private final int i2;
					private final List<? extends Choice> choices;

					public ActionChooseIndex(final int i2, final List<? extends Choice> choices) {
						this.i2 = i2;
						this.choices = choices;
					}

					@Override
					public boolean run(final Context context) {
						if (i2 >= choices.size())
							return false;
						choices.get(i2).choose(context, self.get());
						return true;
					}

					@Override
					public String id() {
						return String.format("choose_%s", i2);
					}
				}
			}

			public GapSelection(
					final Context context, final boolean leadFirst, final int beginOffset, final int endOffset
			) {
				super(context, leadFirst, beginOffset, endOffset);
				self = dataType.get(data);
			}

			public void updateGap(final Context context) {
				if (gapDetails != null) {
					context.details.removePage(context, gapDetails);
					gapDetails.destroy(context);
				}
				final List<? extends Choice> choices = process(context, self.parent.atom(), self.get(), userData);
				ImmutableList
						.copyOf(context.gapChoiceListeners)
						.forEach(listener -> listener.changed(context, choices));
				if (!choices.isEmpty()) {
					gapDetails = new GapDetails(context, choices);
					context.details.addPage(context, gapDetails);
				} else {
					if (gapDetails != null) {
						context.details.removePage(context, gapDetails);
						gapDetails.destroy(context);
						gapDetails = null;
					}
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
					out.add(middle.pattern.build());
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
					if (text.isEmpty() && ((FrontSymbol) front).type instanceof SymbolText)
						text = ((SymbolText) ((FrontSymbol) front).type).text;
					grammar.add("root", Grammar.stringSequence(text));
				} else if (front instanceof FrontDataPrimitive) {
					final MiddlePrimitive middle =
							(MiddlePrimitive) type.middle.get(((FrontDataPrimitive) front).middle);
					grammar.add("root", middle.pattern.build());
				} else
					throw new DeadCode();
				final Pair<ParseContext, Position> longest = new Parse<>()
						.grammar(grammar)
						.longestMatchFromStart(new ByteArrayInputStream(string
								.substring(at)
								.getBytes(StandardCharsets.UTF_8)));
				if (front instanceof FrontDataPrimitive) {
					data.put(front.middle(), new ValuePrimitive(
							type.getDataPrimitive(front.middle()),
							string.substring(at, at + (int) longest.second.distance())
					));
					filled.remove(front.middle());
					out.nextInput = front;
				} else
					out.nextInput = null;
				at = at + (int) longest.second.distance();
				if (at >= string.length())
					break;
			}
			if (at < string.length())
				out.nextInput = null;
			filled.forEach(middle -> data.put(middle, type.middle.get(middle).create(context.syntax)));
			out.remainder = string.substring(at);
			out.atom = new Atom(type, data);

			// Look for the next place to enter text
			if (out.nextInput == null)
				for (final FrontPart part : iterable(frontIterator)) {
					if (!(part instanceof FrontDataPrimitive))
						continue;
					out.nextInput = part;
					break;
				}

			return out;
		}
	}

	protected static List<GapKey> gapKeys(final Syntax syntax, final FreeAtomType type, final AtomType childType) {
		final List<GapKey> out = new ArrayList<>();
		final Common.Mutable<GapKey> top = new Common.Mutable<>(new GapKey());
		top.value.indexBefore = -1;
		enumerate(type.front().stream()).forEach(p -> {
			p.second.dispatch(new FrontPart.DispatchHandler() {
				private void flush(final boolean drop) {
					if (!top.value.keyParts.isEmpty()) {
						if (drop)
							top.value.indexAfter = -1;
						else
							top.value.indexAfter = p.first;
						out.add(top.value);
						top.value = new GapKey();
					}
					if (drop)
						top.value.indexBefore = -1;
					else
						top.value.indexBefore = p.first;
				}

				private boolean isTypeAllowed(final String type) {
					return childType == null ||
							childType == syntax.gap ||
							childType == syntax.prefixGap ||
							childType == syntax.suffixGap ||
							syntax.getLeafTypes(type).anyMatch(t -> t.equals(childType));
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
					flush(!isTypeAllowed(((MiddleArrayBase) type.middle.get(front.middle())).type));
					front.suffix.forEach(front2 -> front2.dispatch(this));
				}

				@Override
				public void handle(final FrontDataAtom front) {
					flush(!isTypeAllowed(((MiddleAtom) type.middle.get(front.middle())).type));
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
