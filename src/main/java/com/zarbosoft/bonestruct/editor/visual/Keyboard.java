package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.editor.model.hid.Key;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Keyboard {

	public static Node ruleFromString(final String name) {
		final Sequence out = new Sequence();
		for (int i = 0; i < name.length(); ++i) {
			try {
				out.add(new com.zarbosoft.pidgoon.events.Terminal(new Keyboard.Event(Key.fromChar(name.charAt(i)),
						false,
						false,
						false
				)));
			} catch (final IllegalArgumentException e) {
				continue;
			}
		}
		return out;
	}

	public static class Event implements com.zarbosoft.pidgoon.events.Event {
		public Event(final Key key, final boolean control, final boolean shift, final boolean alt) {
			this.key = key;
			this.control = control;
			this.shift = shift;
			this.alt = alt;
		}

		Key key;
		boolean control;
		boolean shift;
		boolean alt;

		@Override
		public boolean matches(final com.zarbosoft.pidgoon.events.Event event) {
			final Event keyEvent = (Event) event;
			if (key != keyEvent.key)
				return false;
			if (control != keyEvent.control)
				return false;
			if (shift != keyEvent.shift)
				return false;
			if (alt != keyEvent.alt)
				return false;
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder out = new StringBuilder();
			if (control)
				out.append("ctrl+");
			if (alt)
				out.append("alt+");
			if (shift)
				out.append("ctrl+");
			final Luxem.Configuration annotation =
					uncheck(() -> Key.class.getField(key.name()).getAnnotation(Luxem.Configuration.class));
			out.append(annotation.name());
			return out.toString();
		}
	}

}
