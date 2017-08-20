package com.zarbosoft.bonestruct.modules;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.tags.GlobalTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Pair;

import java.util.List;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;

@Configuration(name = "modes")
public class Modes extends Module {
	@Configuration
	public List<String> states;

	@Override
	public State initialize(final Context context) {
		return new ModuleState(context);
	}

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "modes module";
		}
	}

	private class ModuleState extends State {
		private int state = 0;

		ModuleState(final Context context) {
			context.addActions(this, enumerate(states.stream()).map(pair -> {
				return new ActionMode(pair);
			}).collect(Collectors.toList()));
			context.changeGlobalTags(new TagsChange(ImmutableSet.of(getTag(state)), ImmutableSet.of()));
		}

		@Override
		public void destroy(final Context context) {
			context.changeGlobalTags(new TagsChange(ImmutableSet.of(), ImmutableSet.of(getTag(state))));
			context.removeActions(this);
		}

		private Tag getTag(final int state) {
			return new GlobalTag(String.format("mode_%s", states.get(state)));
		}

		@Action.StaticID(id = "mode_%s (%s = mode id)")
		private class ActionMode extends ActionBase {
			private final Pair<Integer, String> pair;

			public ActionMode(final Pair<Integer, String> pair) {
				this.pair = pair;
			}

			@Override
			public boolean run(final Context context) {
				context.changeGlobalTags(new TagsChange(ImmutableSet.of(getTag(pair.first)),
						ImmutableSet.of(getTag(state))
				));
				state = pair.first;
				return true;
			}

			@Override
			public String id() {
				return String.format("mode_%s", pair.second);
			}
		}
	}
}
