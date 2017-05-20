package com.zarbosoft.bonestruct.modules.hotkeys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.details.DetailsPage;
import com.zarbosoft.bonestruct.editor.display.Group;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.bonestruct.editor.display.derived.TLayout;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.modules.Module;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Parse;
import com.zarbosoft.pidgoon.nodes.Union;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "hotkeys", description = "Trigger actions with keys and key combinations.")
public class Hotkeys extends Module {
	@Configuration(optional = true)
	public List<HotkeyRule> rules = new ArrayList<>();

	@Configuration(name = "show_details", optional = true)
	public boolean showDetails = true;

	private Grammar hotkeyGrammar;
	private EventStream<Action> hotkeyParse;
	private String hotkeySequence = "";
	private HotkeyDetails hotkeyDetails = null;
	Map<String, List<Node>> hotkeys = new HashMap<>();
	boolean freeTyping = true;

	private class HotkeyDetails extends DetailsPage {
		public HotkeyDetails(final Context context) {
			final Group group = context.display.group();
			this.node = group;
			final TLayout layout = new TLayout(group);

			final Text first = context.display.text();
			final Style.Baked firstStyle = context.getStyle(context.globalTags
					.plus(new Visual.PartTag("details_prompt"))
					.plus(new Visual.PartTag("details")));
			first.setColor(context, firstStyle.color);
			first.setFont(context, firstStyle.getFont(context));
			first.setText(context, hotkeySequence);
			layout.add(first);

			final Style.Baked lineStyle = context.getStyle(context.globalTags
					.plus(new Visual.PartTag("details_line"))
					.plus(new Visual.PartTag("details")));
			final ColumnarTableLayout table = new ColumnarTableLayout(context, context.syntax.detailSpan);
			for (final com.zarbosoft.pidgoon.internal.State leaf : hotkeyParse.context().leaves) {
				final Action action = leaf.color();
				final Text rule = context.display.text();
				rule.setColor(context, lineStyle.color);
				rule.setFont(context, lineStyle.getFont(context));
				rule.setText(context, hotkeyGrammar.getNode(action.getName()).toString());
				final Text name = context.display.text();
				name.setColor(context, lineStyle.color);
				name.setFont(context, lineStyle.getFont(context));
				name.setText(context, action.getName());
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

	@Override
	public State initialize(final Context context) {
		context.addKeyListener(this::handleEvent);
		context.addSelectionTagsChangeListener(new Context.TagsListener() {
			@Override
			public void tagsChanged(final Context context, final PSet<Visual.Tag> tags) {
				System.out.format("\n\n\nresetting hotkeys tags [%s]\n", tags);
				clean(context);
				hotkeys = new HashMap<>();
				freeTyping = true;
				for (final HotkeyRule rule : rules) {
					System.out.format("rule [%s]\n", rule.with);
					if (!tags.containsAll(rule.with) || !Sets.intersection(tags, rule.without).isEmpty())
						continue;
					System.out.format("\tmatches\n");
					hotkeys.putAll(rule.hotkeys);
					freeTyping = freeTyping && rule.freeTyping;
				}
				hotkeyGrammar = new Grammar();
				final Union union = new Union();
				for (final Action action : iterable(context.actions
						.entrySet()
						.stream()
						.flatMap(e -> e.getValue().stream()))) {
					if (hotkeys.containsKey(action.getName())) {
						for (final Node hotkey : hotkeys.get(action.getName())) {
							System.out.format("action [%s] adding hotkey [%s]\n", action.getName(), hotkey);
							union.add(new Operator(hotkey.build(), store -> store.pushStack(action)));
						}
					}
				}
				hotkeyGrammar.add("root", union);
			}
		});
		return new State() {
			@Override
			public void destroy(final Context context) {
				clean(context);
			}
		};
	}

	public boolean handleEvent(final Context context, final HIDEvent event) {
		if (hotkeyParse == null) {
			hotkeyParse = new Parse<Action>().grammar(hotkeyGrammar).parse();
		}
		if (hotkeySequence.isEmpty())
			hotkeySequence += event.toString();
		else
			hotkeySequence += " " + event.toString();
		try {
			hotkeyParse = hotkeyParse.push(event, hotkeySequence);
			if (hotkeyParse.ended()) {
				final Action action = hotkeyParse.finish();
				clean(context);
				System.out.format("running [%s]\n", action.getName());
				action.run(context);
			} else {
				if (showDetails) {
					if (hotkeyDetails != null)
						context.details.removePage(context, hotkeyDetails);
					hotkeyDetails = new HotkeyDetails(context);
					context.details.addPage(context, hotkeyDetails);
				}
			}
			System.out.format("key consumed\n");
			return true;
		} catch (final InvalidStream e) {
			clean(context);
			return freeTyping ? false : true;
		}
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
