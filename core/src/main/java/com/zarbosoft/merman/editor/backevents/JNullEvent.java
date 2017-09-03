package com.zarbosoft.merman.editor.backevents;

import com.zarbosoft.pidgoon.events.MatchingEvent;

public class JNullEvent implements BackEvent {
	@Override
	public boolean matches(final MatchingEvent event) {
		return event instanceof JNullEvent;
	}

	@Override
	public String toString() {
		return "null";
	}
}
