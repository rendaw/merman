package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "key")
public class ETypeEvent implements BackEvent {
	public ETypeEvent(final String string) {
		value = string;
	}

	public ETypeEvent() {
	}

	@Configuration
	public String value;

	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof ETypeEvent && (value == null || value.equals(((ETypeEvent) event).value));
	}

	@Override
	public String toString() {
		return String.format("TYPE %s", value == null ? "*" : value);
	}
}
