package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LArrayCloseEvent;
import com.zarbosoft.luxem.read.source.LArrayOpenEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Terminal;
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
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Terminal(new LArrayOpenEvent()));
		for (final BackPart element : elements) {
			sequence.add(element.buildBackRule(syntax, nodeType));
		}
		sequence.add(new Terminal(new LArrayCloseEvent()));
		return sequence;
	}

	@Override
	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		enumerate(elements.stream()).forEach(pair -> {
			pair.second.finish(syntax, nodeType, middleUsed);
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
