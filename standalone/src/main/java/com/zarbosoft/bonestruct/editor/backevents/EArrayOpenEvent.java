package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "array-open")
public class EArrayOpenEvent implements BackEvent {

	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof EArrayOpenEvent;
	}

	@Override
	public String toString() {
		return String.format("ARRAY OPEN");
	}
}
