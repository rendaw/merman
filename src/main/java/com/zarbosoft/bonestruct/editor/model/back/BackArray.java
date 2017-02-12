package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LArrayCloseEvent;
import com.zarbosoft.luxemj.source.LArrayOpenEvent;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.List;
import java.util.Set;

@Luxem.Configuration(name = "array")
public class BackArray extends BackPart {
	@Luxem.Configuration
	public String name;
	@Luxem.Configuration
	public List<BackPart> elements;
	private Parent parent;

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
	public void finish(final Syntax syntax, final FreeNodeType nodeType, final Set<String> middleUsed) {
		Helper.enumerate(elements.stream()).forEach(pair -> {
			pair.second.finish(syntax, nodeType, middleUsed);
			pair.second.parent = new Parent() {
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
