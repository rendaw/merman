package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "array-close")
public class EArrayCloseEvent implements BackEvent {

	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof EArrayCloseEvent;
	}

	@Override
	public String toString() {
		return String.format("ARRAY CLOSE");
	}
}
