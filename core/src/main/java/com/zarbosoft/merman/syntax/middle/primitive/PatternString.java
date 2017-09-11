package com.zarbosoft.merman.syntax.middle.primitive;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.Grammar;

@Configuration(name = "string")
public class PatternString extends Pattern {
	@Configuration
	public String string;

	@Override
	public Node build() {
		return Grammar.stringSequence(string);
	}
}
