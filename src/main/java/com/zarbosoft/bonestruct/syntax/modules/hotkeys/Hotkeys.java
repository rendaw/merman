package com.zarbosoft.bonestruct.syntax.modules.hotkeys;

import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.details.DetailsPage;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.raw.RawText;
import com.zarbosoft.bonestruct.syntax.modules.Module;
import com.zarbosoft.bonestruct.syntax.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Parse;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.scene.Group;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "hotkeys", description = "Trigger actions with keys and key combinations.")
public class Hotkeys extends Module {
	@Configuration(optional = true)
	public List<HotkeyRule> rules = new ArrayList<>();

	@Configuration(optional = true, name = "show_details")
	public boolean showDetails = true;

	private Grammar hotkeyGrammar;
	private EventStream<Action> hotkeyParse;
	private String hotkeySequence = "";
	private HotkeyDetails hotkeyDetails = null;
	private HotkeyRule hotkeys = null;

	private class HotkeyDetails extends DetailsPage {
		public HotkeyDetails(final Context context) {
			final Group group = new Group();
			this.node = group;
			final PSet tags = HashTreePSet.from(context.globalTags);
			final RawText first = new RawText(
					context,
					context.getStyle(tags
							.plus(new Visual.PartTag("details_prompt"))
							.plus(new Visual.PartTag("details")))
			);
			group.getChildren().add(first.getVisual());
			final Style.Baked lineStyle =
					context.getStyle(tags.plus(new Visual.PartTag("details_line")).plus(new Visual.PartTag("details")));
			first.setText(context, hotkeySequence);
			int transverse = first.transverseSpan(context);
			for (final com.zarbosoft.pidgoon.internal.State leaf : hotkeyParse.context().leaves) {
				final RawText line = new RawText(context, lineStyle);
				line.setText(context, ((Action) leaf.color()).getName());
				group.getChildren().add(line.getVisual());
				line.setTransverse(context, transverse);
				transverse += line.transverseSpan(context);
			}
		}
	}

	@Override
	public State initialize(final Context context) {
		context.addKeyListener(this::handleEvent);
		context.addTagsChangeListener(new Context.TagsListener() {
			@Override
			public void tagsChanged(final Context context, final Set<Visual.Tag> tags) {
				System.out.format("resetting hotkeys tags [%s]\n", tags);
				clean(context);
				hotkeys = new HotkeyRule(tags);
				for (final HotkeyRule rule : rules) {
					System.out.format("rule [%s]\n", rule.tags);
					if (!tags.containsAll(rule.tags))
						continue;
					System.out.format("\tmatches\n");
					hotkeys.merge(rule);
				}
				hotkeyGrammar = new Grammar();
				final Union union = new Union();
				for (final Action action : iterable(context.actions
						.entrySet()
						.stream()
						.flatMap(e -> e.getValue().stream()))) {
					if (hotkeys.hotkeys.containsKey(action.getName())) {
						for (final Node hotkey : hotkeys.hotkeys.get(action.getName())) {
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
		final boolean clean = false;
		try {
			hotkeyParse = hotkeyParse.push(event, hotkeySequence);
			if (hotkeyParse.ended()) {
				final Action action = hotkeyParse.finish();
				clean(context);
				System.out.format("running [%s]\n", action.getName());
				action.run(context);
			} else {
				if (showDetails && context.display != null) {
					if (hotkeyDetails != null)
						context.display.details.removePage(context, hotkeyDetails);
					hotkeyDetails = new HotkeyDetails(context);
					context.display.details.addPage(context, hotkeyDetails);
				}
			}
			System.out.format("key consumed\n");
			return true;
		} catch (final InvalidStream e) {
			clean(context);
			return hotkeys.freeTyping ? false : true;
		}
	}

	private void clean(final Context context) {
		hotkeySequence = "";
		hotkeyParse = null;
		if (hotkeyDetails != null) {
			context.display.details.removePage(context, hotkeyDetails);
			hotkeyDetails = null;
		}
	}
}
