package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LArrayCloseEvent;
import com.zarbosoft.luxemj.source.LArrayOpenEvent;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.List;

@Luxem.Configuration(name = "array")
public class BackArray implements BackPart {
	@Luxem.Configuration
	public String name;
	@Luxem.Configuration
	public List<BackPart> elements;

	@Override
	public Node buildLoadRule(final Syntax syntax) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Terminal(new LArrayOpenEvent()));
		for (final BackPart element : elements) {
			sequence.add(element.buildLoadRule(syntax));
		}
		sequence.add(new Terminal(new LArrayCloseEvent()));
		return sequence;
	}

	@Override
	public void finish(final NodeType nodeType, final java.util.Set<String> middleUsed) {
		elements.forEach(v -> v.finish(nodeType, middleUsed));
	}
}
