package com.zarbosoft.bonestruct.editor.hid;

import com.zarbosoft.bonestruct.modules.hotkeys.Key;
import com.zarbosoft.interface1.Walk;
import com.zarbosoft.pidgoon.events.MatchingEvent;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.HashSet;
import java.util.Set;

public class HIDEvent implements MatchingEvent {
	public HIDEvent(final Key key, final boolean press, final Set<Key> modifiers) {
		this.key = key;
		this.press = press;
	}

	public final Key key;
	public final boolean press;
	public Set<Key> modifiers = new HashSet<>();

	@Override
	public boolean matches(final MatchingEvent event) {
		throw new DeadCode();
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
