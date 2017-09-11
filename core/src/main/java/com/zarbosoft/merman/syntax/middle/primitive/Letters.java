package com.zarbosoft.merman.syntax.middle.primitive;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.Terminal;

@Configuration(name = "letters")
public class Letters extends Pattern {
	@Override
	public Node build() {
		return new Terminal(ImmutableRangeSet.<Byte>builder()
				.add(Range.closed((byte) 'a', (byte) 'z'))
				.add(Range.closed((byte) 'A', (byte) 'Z'))
				.build());
	}
}
