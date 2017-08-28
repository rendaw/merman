package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "string")
public class JFloatEvent implements BackEvent {
	public JFloatEvent(final String value) {
		this.value = value;
	}

	public JFloatEvent() {
	}

	@Configuration(optional = true)
	public String value = null;

	@Override
	public boolean matches(final MatchingEvent event) {
		if (value == null)
			return event instanceof JFloatEvent || event instanceof JNullEvent;
		else
			return event instanceof JFloatEvent && value.equals(((JFloatEvent) event).value);
	}

	@Override
	public String toString() {
		if (value == null)
			return "(float) *";
		return value;
	}
}
