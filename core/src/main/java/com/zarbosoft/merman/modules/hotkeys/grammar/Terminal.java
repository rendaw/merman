package com.zarbosoft.merman.modules.hotkeys.grammar;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.modules.hotkeys.Key;
import com.zarbosoft.pidgoon.events.Event;

import java.util.HashSet;
import java.util.Set;

@Configuration(name = "key")
public class Terminal implements Node {
	@Configuration
	public Key key;

	@Configuration(optional = true)
	public boolean press = true;

	@Configuration(optional = true)
	public Set<Key> modifiers = new HashSet<>();

	public com.zarbosoft.pidgoon.Node build() {
		return new com.zarbosoft.pidgoon.events.Terminal() {
			@Override
			protected boolean matches(final Event event) {
				final HIDEvent event1 = (HIDEvent) event;
				final boolean a = key.equals(event1.key);
				final boolean b = press == event1.press;
				final boolean c = modifiers.equals(event1.modifiers);
				return a && b && c;
			}
		};
	}

	@Override
	public String toString() {
		return key.toString();
	}
}
