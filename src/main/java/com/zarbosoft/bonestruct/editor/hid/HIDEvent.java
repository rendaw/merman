package com.zarbosoft.bonestruct.editor.hid;

import com.zarbosoft.bonestruct.modules.hotkeys.Key;
import com.zarbosoft.interface1.Walk;

public class HIDEvent implements com.zarbosoft.pidgoon.events.Event {
	public HIDEvent(final Key key, final boolean press) {
		this.key = key;
		this.press = press;
	}

	public final Key key;
	public final boolean press;

	@Override
	public boolean matches(final com.zarbosoft.pidgoon.events.Event event) {
		final HIDEvent keyEvent = (HIDEvent) event;
		if (key != keyEvent.key)
			return false;
		if (press != keyEvent.press)
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder();
		if (!press)
			out.append("↑");
		out.append(Walk.decideEnumName(key));
		return out.toString();
	}
}
