package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.editor.backevents.JFalseEvent;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;

@Configuration(name = "json_false")
public class BackJSONFalse extends BackPart {

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new MatchingEventTerminal(new JFalseEvent());
	}
}
