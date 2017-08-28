package com.zarbosoft.bonestruct.editor.backevents;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.MatchingEvent;

@Configuration(name = "primitive")
public class EPrimitiveEvent implements BackEvent {
	public EPrimitiveEvent(final String value) {
		this.value = value;
	}

	public EPrimitiveEvent() {
	}

	@Configuration(optional = true)
	public String value = null;

	@Override
	public boolean matches(final MatchingEvent event) {
		if (value == null)
			return event instanceof EPrimitiveEvent || event instanceof JNullEvent;
		else
			return event instanceof EPrimitiveEvent && value.equals(((EPrimitiveEvent) event).value);
	}

	@Override
	public String toString() {
		if (value == null)
			return "*";
		return String.format("\"%s\"", value);
	}
}
