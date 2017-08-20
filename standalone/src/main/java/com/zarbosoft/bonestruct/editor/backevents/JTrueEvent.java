package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class JTrueEvent implements BackEvent {
	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof JTrueEvent;
	}

	@Override
	public String toString() {
		return "(bool) true";
	}
}
