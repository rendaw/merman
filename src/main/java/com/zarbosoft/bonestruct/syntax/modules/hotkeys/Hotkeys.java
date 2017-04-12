package com.zarbosoft.bonestruct.syntax.modules.hotkeys;

import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Keyboard;
import com.zarbosoft.bonestruct.editor.details.DetailsPage;
import com.zarbosoft.bonestruct.editor.visual.raw.RawText;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.syntax.hid.Key;
import com.zarbosoft.bonestruct.syntax.modules.Module;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Parse;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.lang.ref.WeakReference;
import java.util.*;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "rules", description = "Trigger actions with keys and key combinations.")
public class Hotkeys extends Module {
	@Configuration(optional = true)
	public List<HotkeyRule> rules = new ArrayList<>();

	@Configuration(optional = true, name = "show-details")
	public boolean showDetails = true;

	public WeakHashMap<Set<VisualNode.Tag>, WeakReference<HotkeyRule>> hotkeysCache = new WeakHashMap<>();
	public Grammar hotkeyGrammar;
	public EventStream<Action> hotkeyParse;
	public String hotkeySequence = "";
	private HotkeyDetails hotkeyDetails = null;

	private class HotkeyDetails extends DetailsPage {
		public HotkeyDetails(final Context context) {
			final Group group = new Group();
			this.node = group;
			final PSet tags = HashTreePSet.from(context.globalTags);
			final RawText first =
					new RawText(context, context.getStyle(tags.plus(new VisualNode.PartTag("details-prompt"))));
			group.getChildren().add(first.getVisual());
			final Style.Baked lineStyle = context.getStyle(tags.plus(new VisualNode.PartTag("details-line")));
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
		context.addKeyListener(this::handleKey);
		context.addTagsChangeListener(new Context.TagsListener() {
			@Override
			public void tagsChanged(final Context context, final Set<VisualNode.Tag> tags) {
				clean(context);
				final HotkeyRule hotkeys;
				{
					final Optional<HotkeyRule> found = hotkeysCache
							.entrySet()
							.stream()
							.filter(e -> tags.equals(e.getKey()))
							.map(e -> e.getValue().get())
							.filter(v -> v != null)
							.findFirst();
					if (found.isPresent())
						hotkeys = found.get();
					else {
						hotkeys = new HotkeyRule(tags);
						for (final HotkeyRule rule : rules) {
							if (tags.containsAll(rule.tags)) {
								hotkeys.merge(rule);
							}
							hotkeysCache.put(hotkeys.tags, new WeakReference<>(hotkeys));
						}
					}
				}
				hotkeyGrammar = new Grammar();
				final Union union = new Union();
				for (final Action action : iterable(context.actions
						.entrySet()
						.stream()
						.flatMap(e -> e.getValue().stream()))) {
					if (hotkeys.hotkeys.containsKey(action.getName())) {
						for (final com.zarbosoft.bonestruct.syntax.hid.grammar.Node hotkey : hotkeys.hotkeys.get(action.getName())) {
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

			}
		};
	}

	public boolean handleKey(final Context context, final KeyEvent event) {
		if (hotkeyParse == null) {
			hotkeyParse = new Parse<Action>().grammar(hotkeyGrammar).parse();
		}
		final Keyboard.Event keyEvent = new Keyboard.Event(Key.fromJFX(event.getCode()),
				event.isControlDown(),
				event.isAltDown(),
				event.isShiftDown()
		);
		if (hotkeySequence.isEmpty())
			hotkeySequence += keyEvent.toString();
		else
			hotkeySequence += " " + keyEvent.toString();
		final boolean clean = false;
		try {
			hotkeyParse = hotkeyParse.push(keyEvent, hotkeySequence);
			if (hotkeyParse.ended()) {
				final Action action = hotkeyParse.finish();
				clean(context);
				action.run(context);
			} else {
				if (showDetails && context.display != null) {
					if (hotkeyDetails != null)
						context.display.details.removePage(context, hotkeyDetails);
					hotkeyDetails = new HotkeyDetails(context);
					context.display.details.addPage(context, hotkeyDetails);
				}
			}
			return false;
		} catch (final InvalidStream e) {
			clean(context);
			return true;
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
