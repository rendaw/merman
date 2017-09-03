package com.zarbosoft.merman.editor.hid;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.interface1.Walk;
import com.zarbosoft.merman.modules.hotkeys.Key;
import com.zarbosoft.pidgoon.events.Event;

import java.util.Set;
import java.util.stream.Collectors;

public class HIDEvent implements Event {
	public HIDEvent(final Key key, final boolean press, final Set<Key> modifiers) {
		this.key = key;
		this.press = press;
		this.modifiers = ImmutableSet.copyOf(modifiers);
	}

	public final Key key;
	public final boolean press;
	public final Set<Key> modifiers;

	@Override
	public String toString() {
		final StringBuilder out = new StringBuilder();
		if (!press)
			out.append("↑");
		out.append(Walk.decideEnumName(key));
		if (!modifiers.isEmpty())
			out.append(String.format(
					" [%s]",
					modifiers
							.stream()
							.map(modifier -> "+" + Walk.decideEnumName(modifier))
							.collect(Collectors.joining(" "))
			));
		return out.toString();
	}
}
