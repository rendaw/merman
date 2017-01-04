package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LKeyEvent;
import com.zarbosoft.luxemj.source.LObjectCloseEvent;
import com.zarbosoft.luxemj.source.LObjectOpenEvent;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Set;

import java.util.Map;

@Luxem.Configuration(name = "record")
public class BackRecord implements BackPart {
	@Luxem.Configuration
	public Map<String, BackPart> pairs;

	@Override
	public Node buildLoadRule() {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Terminal(new LObjectOpenEvent()));
		final Set set = new Set();
		pairs.forEach((key, value) -> {
			set.add(new Sequence().add(new Terminal(new LKeyEvent(key))).add(value.buildLoadRule()));
		});
		sequence.add(set);
		sequence.add(new Terminal(new LObjectCloseEvent()));
		return sequence;
	}

	@Override
	public void finish(final NodeType nodeType, final java.util.Set<String> middleUsed) {
		pairs.forEach((k, v) -> v.finish(nodeType, middleUsed));
	}
}
