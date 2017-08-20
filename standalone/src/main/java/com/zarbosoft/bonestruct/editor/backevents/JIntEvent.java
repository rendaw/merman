package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "string")
public class JIntEvent implements BackEvent {
	public JIntEvent(final String value) {
		this.value = value;
	}

	public JIntEvent() {
	}

	@Configuration(optional = true)
	public String value = null;

	@Override
	public boolean matches(final MatchingEvent event) {
		if (value == null)
			return event instanceof JIntEvent || event instanceof JNullEvent;
		else
			return event instanceof JIntEvent && value.equals(((JIntEvent) event).value);
	}

	@Override
	public String toString() {
		if (value == null)
			return "(int) *";
		return String.format("(int) %s", value);
	}
}
