package com.zarbosoft.merman.syntax.middle.primitive;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.bytes.Grammar;
import com.zarbosoft.pidgoon.bytes.Parse;

@Configuration
public abstract class Pattern {
	public abstract Node build();

	public class Matcher {
		private final Grammar grammar;

		public Matcher() {
			grammar = new Grammar();
			grammar.add("root", build());
		}

		public boolean match(final String value) {
			try {
				new Parse<Void>().grammar(grammar).parse(value);
				return true;
			} catch (final InvalidStream e) {
				return false;
			}
		}
	}

	public static Pattern repeatedAny;

	static {
		repeatedAny = new Repeat0();
		((Repeat0) repeatedAny).pattern = new Any();
	}
}
