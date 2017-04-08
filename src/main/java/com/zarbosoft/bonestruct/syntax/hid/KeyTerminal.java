package com.zarbosoft.bonestruct.syntax.hid;

import com.zarbosoft.bonestruct.editor.Keyboard;
import com.zarbosoft.bonestruct.syntax.hid.grammar.Terminal;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "key")
public class KeyTerminal extends Terminal {
	@Configuration
	public Key key;
	@Configuration(optional = true)
	public boolean control = false;
	@Configuration(optional = true)
	public boolean shift = false;
	@Configuration(optional = true)
	public boolean alt = false;

	@Override
	public com.zarbosoft.pidgoon.events.Event getEvent() {
		return new Keyboard.Event(key, control, shift, alt);
	}
}
