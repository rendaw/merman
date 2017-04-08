package com.zarbosoft.bonestruct.syntax.hid.grammar;

import com.zarbosoft.interface1.Configuration;

import java.util.List;

@Configuration(name = "or")
public class Union implements Node {
	@Configuration
	public List<Node> nodes;

	@Override
	public com.zarbosoft.pidgoon.Node build() {
		final com.zarbosoft.pidgoon.nodes.Union out = new com.zarbosoft.pidgoon.nodes.Union();
		for (final Node node : nodes)
			out.add(node.build());
		return out;
	}
}
