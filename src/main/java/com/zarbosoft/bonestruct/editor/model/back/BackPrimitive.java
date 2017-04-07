package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Terminal;

@Configuration(name = "primitive")
public class BackPrimitive extends BackPart {
	@Configuration
	public String value;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		return new Terminal(new LPrimitiveEvent(value));
	}
}
