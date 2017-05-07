package com.zarbosoft.bonestruct.modules.hotkeys;

import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Terminal;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "key")
public class HIDTerminal extends Terminal {
	@Configuration
	public Key key;

	@Configuration(optional = true, description = "True if the input is pressed, false if the input is released.")
	public boolean press = true;

	@Override
	public com.zarbosoft.pidgoon.events.Event getEvent() {
		return new HIDEvent(key, press);
	}

	@Override
	public String toString() {
		return key.toString();
	}
}
