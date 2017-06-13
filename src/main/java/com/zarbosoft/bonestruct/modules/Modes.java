package com.zarbosoft.bonestruct.modules;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.tags.GlobalTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.interface1.Configuration;

import java.util.List;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;

@Configuration(name = "modes", description = "Adds actions to change global tags.")
public class Modes extends Module {
	@Configuration
	public List<String> states;

	private int state = 0;

	private Tag getTag(final int state) {
		return new GlobalTag(String.format("mode_%s", states.get(state)));
	}

	@Override
	public State initialize(final Context context) {
		context.addActions(this, enumerate(states.stream()).map(pair -> {
			return new Action() {
				@Override
				public boolean run(final Context context) {
					context.changeGlobalTags(new TagsChange(ImmutableSet.of(getTag(pair.first)),
							ImmutableSet.of(getTag(state))
					));
					state = pair.first;
					return true;
				}

				@Override
				public String getName() {
					return String.format("mode_%s", pair.second);
				}
			};
		}).collect(Collectors.toList()));
		context.changeGlobalTags(new TagsChange(ImmutableSet.of(getTag(state)), ImmutableSet.of()));
		return new State() {
			@Override
			public void destroy(final Context context) {
				context.changeGlobalTags(new TagsChange(ImmutableSet.of(), ImmutableSet.of(getTag(state))));
				context.removeActions(this);
			}
		};
	}
}
