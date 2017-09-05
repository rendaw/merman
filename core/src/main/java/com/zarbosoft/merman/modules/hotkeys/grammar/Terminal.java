package com.zarbosoft.merman.modules.hotkeys.grammar;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.modules.hotkeys.Key;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.internal.Helper;

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
		return new Operator(new com.zarbosoft.pidgoon.events.Terminal() {
			@Override
			protected boolean matches(final Event event) {
				final HIDEvent event1 = (HIDEvent) event;
				final boolean a = key.equals(event1.key);
				final boolean b = press == event1.press;
				final boolean c = event1.modifiers.containsAll(modifiers);
				return a && b && c;
			}
		}, store -> Helper.stackSingleElement(store.pushStack(1 + modifiers.size())));
	}

	@Override
	public String toString() {
		return key.toString();
	}
}
