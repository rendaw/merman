package com.zarbosoft.merman.syntax.middle.primitive;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Wildcard;

@Configuration(name = "any")
public class Any extends Pattern {
	@Override
	public Node build() {
		return new Wildcard();
	}
}
