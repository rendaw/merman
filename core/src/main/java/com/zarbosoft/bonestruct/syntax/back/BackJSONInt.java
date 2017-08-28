package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.editor.backevents.JIntEvent;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;

@Configuration(name = "json_int")
public class BackJSONInt extends BackPart {
	@Configuration
	public String value;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new MatchingEventTerminal(new JIntEvent(value));
	}
}
