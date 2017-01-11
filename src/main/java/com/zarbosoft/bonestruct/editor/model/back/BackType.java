package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LTypeEvent;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;

@Luxem.Configuration(name = "type")
public class BackType implements BackPart {
	@Luxem.Configuration
	public String value;

	@Override
	public Node buildLoadRule() {
		return new Terminal(new LTypeEvent(value));
	}
}
