package com.zarbosoft.bonestruct.syntax.modules;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.interface1.Configuration;

import java.util.List;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;

@Configuration(name = "modes", description = "Adds actions to change global tags.")
public class Modes extends Module {
	@Configuration
	public List<String> states;

	private int state = 0;

	@Override
	public State initialize(final Context context) {
		context.actions.put(this, enumerate(states.stream()).map(pair -> {
			return new Action() {
				@Override
				public void run(final Context context) {
					context.changeGlobalTags(new VisualNode.TagsChange(ImmutableSet.of(new VisualNode.GlobalTag(states.get(
							pair.first))),
							ImmutableSet.of(new VisualNode.GlobalTag(states.get(state)))
					));
					state = pair.first;
				}

				@Override
				public String getName() {
					return String.format("mode_%s", pair.second);
				}
			};
		}).collect(Collectors.toList()));
		context.changeGlobalTags(new VisualNode.TagsChange(ImmutableSet.of(new VisualNode.GlobalTag(states.get(state))),
				ImmutableSet.of()
		));
		return new State() {
			@Override
			public void destroy(final Context context) {
				context.changeGlobalTags(new VisualNode.TagsChange(ImmutableSet.of(),
						ImmutableSet.of(new VisualNode.GlobalTag(states.get(state)))
				));
				context.actions.remove(this);
			}
		};
	}
}
