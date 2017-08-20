package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "object-open")
public class EObjectOpenEvent implements BackEvent {

	@Override
	public boolean matches(final MatchingEvent event) {
		return event.getClass() == getClass();
	}

	@Override
	public String toString() {
		return String.format("OBJECT OPEN");
	}
}
