package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LTypeEvent;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;

@Luxem.Configuration(name = "type")
public class BackType extends BackPart {
	@Luxem.Configuration
	public String value;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		return new Terminal(new LTypeEvent(value));
	}
}
