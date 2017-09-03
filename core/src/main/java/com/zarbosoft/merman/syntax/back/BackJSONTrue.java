package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.backevents.JTrueEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;

@Configuration(name = "json_true")
public class BackJSONTrue extends BackPart {

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new MatchingEventTerminal(new JTrueEvent());
	}
}
