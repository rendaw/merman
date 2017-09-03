package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "key")
public class EKeyEvent implements BackEvent {
	public EKeyEvent(final String string) {
		value = string;
	}

	public EKeyEvent() {
	}

	@Configuration
	public String value;

	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof EKeyEvent && (value == null || value.equals(((EKeyEvent) event).value));
	}

	@Override
	public String toString() {
		return String.format("KEY %s", value == null ? "*" : value);
	}
}
