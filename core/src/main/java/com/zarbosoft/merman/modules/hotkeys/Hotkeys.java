package com.zarbosoft.merman.modules.hotkeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.details.DetailsPage;
import com.zarbosoft.merman.editor.display.Group;
import com.zarbosoft.merman.editor.display.Text;
import com.zarbosoft.merman.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.merman.editor.display.derived.TLayout;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.modules.Module;
import com.zarbosoft.merman.modules.hotkeys.grammar.Node;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.*;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.rendaw.common.ChainComparator;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.*;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "hotkeys")
public class Hotkeys extends Module {
	@Configuration(optional = true)
	public List<HotkeyRule> rules = new ArrayList<>();

	@Configuration(name = "show_details", optional = true)
	public boolean showDetails = true;

	@Override
	public State initialize(final Context context) {
		return new ModuleState(context);
	}

	private static final Comparator<Pair<Integer, Action>> matchComparator =
			new ChainComparator<Pair<Integer, Action>>().greaterFirst(p -> p.first).build();

	private class ModuleState extends State {
		private class HotkeyDetails extends DetailsPage {
			public HotkeyDetails(final Context context) {
				final Group group = context.display.group();
				this.node = group;
				final TLayout layout = new TLayout(group);

				final Text first = context.display.text();
				final Style.Baked firstStyle = context.getStyle(context.globalTags
						.plus(new PartTag("details_prompt"))
						.plus(new PartTag("details")));
				first.setColor(context, firstStyle.color);
				first.setFont(context, firstStyle.getFont(context));
				first.setText(context, hotkeySequence);
				layout.add(first);

				final Style.Baked lineStyle = context.getStyle(context.globalTags
						.plus(new PartTag("details_line"))
						.plus(new PartTag("details")));
				final ColumnarTableLayout table = new ColumnarTableLayout(context, context.syntax.detailSpan);
				for (final com.zarbosoft.pidgoon.internal.State leaf : hotkeyParse.context().leaves) {
					final Action action = leaf.color();
					final Text rule = context.display.text();
					rule.setColor(context, lineStyle.color);
					rule.setFont(context, lineStyle.getFont(context));
					rule.setText(context, hotkeyGrammar.getNode(action.id()).toString());
					final Text name = context.display.text();
					name.setColor(context, lineStyle.color);
					name.setFont(context, lineStyle.getFont(context));
					name.setText(context, action.id());
					table.add(ImmutableList.of(rule, name));
				}
				table.layout(context);
				layout.add(table.group);
				layout.layout(context);
			}

			@Override
			public void tagsChanged(final Context context) {

			}
		}

		private Grammar hotkeyGrammar;
		private EventStream<Pair<Integer, Action>> hotkeyParse;
		private String hotkeySequence = "";
		private HotkeyDetails hotkeyDetails = null;
		Map<String, List<Node>> hotkeys = new HashMap<>();
		boolean freeTyping = true;

		public ModuleState(final Context context) {
			context.addKeyListener(this::handleEvent);
			final Context.TagsListener tagsListener = new Context.TagsListener() {
				@Override
				public void tagsChanged(final Context context) {
					update(context);
				}
			};
			context.addSelectionTagsChangeListener(tagsListener);
			context.addGlobalTagsChangeListener(tagsListener);
			context.addActionChangeListener(new Context.ActionChangeListener() {
				@Override
				public void actionsAdded(final Context context) {
					update(context);
				}

				@Override
				public void actionsRemoved(final Context context) {
					update(context);
				}
			});
		}

		@Override
		public void destroy(final Context context) {
			clean(context);
		}

		private void update(final Context context) {
			if (context.selection == null)
				return;
			final PSet<Tag> tags = context.globalTags.plusAll(context.selection.getTags(context));
			clean(context);
			hotkeys = new HashMap<>();
			freeTyping = true;
			for (final HotkeyRule rule : rules) {
				if (!tags.containsAll(rule.with) || !Sets.intersection(tags, rule.without).isEmpty())
					continue;
				hotkeys.putAll(rule.hotkeys);
				freeTyping = freeTyping && rule.freeTyping;
			}
			hotkeyGrammar = new Grammar();
			final Union union = new Union();
			for (final Action action : iterable(context.actions())) {
				if (hotkeys.containsKey(action.id())) {
					for (final Node hotkey : hotkeys.get(action.id())) {
						union.add(new Operator(hotkey.build(), store -> {
							final Pair<Integer, Action> out = new Pair<>(0, action);
							store = (Store) Helper.<Integer>stackPopSingleList(store, i -> {
								out.first += i;
							});
							return store.pushStack(out);
						}));
					}
				}
			}
			hotkeyGrammar.add("root", union);
		}

		public boolean handleEvent(final Context context, final HIDEvent event) {
			if (hotkeyParse == null) {
				hotkeyParse = new Parse<Pair<Integer, Action>>().grammar(hotkeyGrammar).stack(() -> 0).parse();
			}
			if (hotkeySequence.isEmpty())
				hotkeySequence += event.toString();
			else
				hotkeySequence += " " + event.toString();
			boolean result;
			try {
				hotkeyParse = hotkeyParse.push(event, hotkeySequence);
				if (hotkeyParse.ended()) {
					final Action action =
							hotkeyParse.finishAll().stream().sorted(matchComparator).findFirst().get().second;
					clean(context);
					action.run(context);
				} else {
					if (showDetails) {
						if (hotkeyDetails != null)
							context.details.removePage(context, hotkeyDetails);
						hotkeyDetails = new HotkeyDetails(context);
						context.details.addPage(context, hotkeyDetails);
					}
				}
				result = true;
			} catch (final InvalidStream e) {
				clean(context);
				result = freeTyping ? false : true;
			}
			return result;
		}

		private void clean(final Context context) {
			hotkeySequence = "";
			hotkeyParse = null;
			if (hotkeyDetails != null) {
				context.details.removePage(context, hotkeyDetails);
				hotkeyDetails = null;
			}
		}
	}

}
