package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.bonestruct.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.enumerate;

@Configuration(name = "array")
public class BackArray extends BackPart {
	@Configuration
	public String name;
	@Configuration
	public List<BackPart> elements = new ArrayList<>();

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new MatchingEventTerminal(new EArrayOpenEvent()));
		for (final BackPart element : elements) {
			sequence.add(element.buildBackRule(syntax, atomType));
		}
		sequence.add(new MatchingEventTerminal(new EArrayCloseEvent()));
		return sequence;
	}

	@Override
	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		enumerate(elements.stream()).forEach(pair -> {
			pair.second.finish(syntax, atomType, middleUsed);
			pair.second.parent = new PartParent() {
				@Override
				public BackPart part() {
					return BackArray.this;
				}

				@Override
				public String pathSection() {
					return pair.first.toString();
				}
			};
		});
	}
}
