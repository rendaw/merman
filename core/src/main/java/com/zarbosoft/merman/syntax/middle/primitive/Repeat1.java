package com.zarbosoft.merman.syntax.middle.primitive;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;

@Configuration(name = "rep1")
public class Repeat1 extends Pattern {
	@Configuration
	public Pattern pattern;

	@Override
	public Node build() {
		return new Repeat(pattern.build()).min(1);
	}
}
