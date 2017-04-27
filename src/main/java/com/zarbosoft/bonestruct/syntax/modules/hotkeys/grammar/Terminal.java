package com.zarbosoft.bonestruct.syntax.modules.hotkeys.grammar;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.Event;

@Configuration(name = "terminal")
public abstract class Terminal implements Node {
	@Override
	public com.zarbosoft.pidgoon.Node build() {
		return new com.zarbosoft.pidgoon.events.Terminal(getEvent());
	}

	public abstract Event getEvent();
}
