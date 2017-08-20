package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class JFalseEvent implements BackEvent {
	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof JFalseEvent;
	}

	@Override
	public String toString() {
		return "(bool) false";
	}
}
