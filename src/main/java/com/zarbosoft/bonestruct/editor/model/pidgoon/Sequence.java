package com.zarbosoft.bonestruct.editor.model.pidgoon;

import com.zarbosoft.interface1.Configuration;

import java.util.List;

@Configuration(name = "seq")
public class Sequence implements Node {
	@Configuration
	public List<Node> nodes;

	@Override
	public com.zarbosoft.pidgoon.Node build() {
		final com.zarbosoft.pidgoon.nodes.Sequence out = new com.zarbosoft.pidgoon.nodes.Sequence();
		for (final Node node : nodes)
			out.add(node.build());
		return out;
	}
}
