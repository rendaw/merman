package com.zarbosoft.merman.syntax.middle.primitive;

import com.google.common.collect.Range;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.Terminal;

@Configuration(name = "digits")
public class Digits extends Pattern {
	@Override
	public Node build() {
		return new Terminal(Range.closed((byte) '0', (byte) '9'));
	}
}
