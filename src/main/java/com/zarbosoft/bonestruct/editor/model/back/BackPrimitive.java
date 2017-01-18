package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;

@Luxem.Configuration(name = "primitive")
public class BackPrimitive implements BackPart {
	@Luxem.Configuration
	public String value;

	@Override
	public Node buildLoadRule(final Syntax syntax) {
		return new Terminal(new LPrimitiveEvent(value));
	}
}
