package com.zarbosoft.bonestruct.modules.hotkeys.grammar;

import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.modules.hotkeys.Key;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.Event;

import java.util.HashSet;
import java.util.Set;

@Configuration(name = "key")
public class Terminal implements Node {
	@Configuration
	public Key key;

	@Configuration(optional = true, description = "True if the input is pressed, false if the input is released.")
	public boolean press = true;

	@Configuration(optional = true, description = "Inputs that must be active for this rule to match.")
	public Set<Key> modifiers = new HashSet<>();
	@Configuration(name = "without_modifiers", optional = true,
			description = "Inputs that must not be active for this rule to match.")
	public Set<Key> withoutModifiers = new HashSet<>();

	public com.zarbosoft.pidgoon.Node build() {
		return new com.zarbosoft.pidgoon.events.Terminal() {
			@Override
			protected boolean matches(final Event event) {
				final HIDEvent event1 = (HIDEvent) event;
				return (
						key.equals(event1.key) &&
								press == event1.press &&
								event1.modifiers.containsAll(modifiers) &&
								Sets.intersection(event1.modifiers, withoutModifiers).isEmpty()
				);
			}
		};
	}

	@Override
	public String toString() {
		return key.toString();
	}
}
