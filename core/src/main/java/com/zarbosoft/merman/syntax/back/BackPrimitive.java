package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.backevents.EPrimitiveEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;

@Configuration(name = "primitive")
public class BackPrimitive extends BackPart {
	@Configuration
	public String value;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new MatchingEventTerminal(new EPrimitiveEvent(value));
	}
}
