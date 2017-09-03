package com.zarbosoft.merman.editor.serialization.json.path;

import com.zarbosoft.merman.editor.backevents.*;

public abstract class JSONPath {

	public JSONPath parent;

	public abstract JSONPath value();

	public abstract JSONPath key(String data);

	public abstract JSONPath type();

	public JSONPath pop() {
		return parent;
	}

	public JSONPath push(final BackEvent e) {
		if (e.getClass() == EArrayOpenEvent.class) {
			return new JSONArrayPath(value());
		} else if (e.getClass() == EArrayCloseEvent.class) {
			return pop();
		} else if (e.getClass() == EObjectOpenEvent.class) {
			return new JSONObjectPath(value());
		} else if (e.getClass() == EObjectCloseEvent.class) {
			return pop();
		} else if (e.getClass() == EKeyEvent.class) {
			return key(((EKeyEvent) e).value);
		} else if (e.getClass() == EPrimitiveEvent.class) {
			return value();
		} else
			throw new AssertionError(String.format("Unknown JSON event type [%s]", e.getClass()));
	}
}
