package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.history.Change;
import com.zarbosoft.bonestruct.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.bonestruct.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePrimitive;

import java.util.HashSet;
import java.util.Set;

public class ValuePrimitive extends com.zarbosoft.bonestruct.document.values.Value {
	public final MiddlePrimitive middle;
	public StringBuilder data = new StringBuilder();
	public final Set<Listener> listeners = new HashSet<>();

	public ValuePrimitive(final MiddlePrimitive middle, final String data) {
		this.middle = middle;
		this.data = new StringBuilder(data);
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public String get() {
		return data.toString();
	}

	public int length() {
		return data.length();
	}

	@Override
	public MiddleElement middle() {
		return middle;
	}

	public Change changeRemove(final int begin, final int length) {
		return new ChangePrimitiveRemove(this, begin, length);
	}

	public Change changeAdd(final int begin, final String text) {
		return new ChangePrimitiveAdd(this, begin, text);
	}

	public abstract static class Listener {
		public abstract void set(Context context, String value);

		public abstract void added(Context context, int index, String value);

		public abstract void removed(Context context, int index, int count);
	}

}
