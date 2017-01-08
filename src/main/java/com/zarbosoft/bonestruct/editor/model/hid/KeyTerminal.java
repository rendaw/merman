package com.zarbosoft.bonestruct.editor.model.hid;

import com.zarbosoft.bonestruct.editor.visual.Keyboard;
import com.zarbosoft.luxemj.Luxem;

@Luxem.Configuration(name = "key")
public class KeyTerminal extends com.zarbosoft.luxemj.grammar.Terminal {
	@Luxem.Configuration
	public Key key;
	@Luxem.Configuration(optional = true)
	public boolean control = false;
	@Luxem.Configuration(optional = true)
	public boolean shift = false;
	@Luxem.Configuration(optional = true)
	public boolean alt = false;

	@Override
	public com.zarbosoft.pidgoon.events.Event getEvent() {
		return new Keyboard.Event(key, control, shift, alt);
	}
}
