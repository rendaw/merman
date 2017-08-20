package com.zarbosoft.bonestruct.editor.serialization.json.path;

public class JSONArrayPath extends JSONPath {

	private boolean type = false;
	private int index = -1;

	public JSONArrayPath(final JSONPath parent) {
		this.parent = parent;
	}

	public JSONArrayPath(final JSONPath parent, final boolean type, final int index) {
		this.parent = parent;
		this.type = type;
		this.index = index;
	}

	@Override
	public JSONPath value() {
		if (this.type)
			return new JSONArrayPath(parent, false, index);
		else
			return new JSONArrayPath(parent, false, index + 1);
	}

	@Override
	public JSONPath key(final String data) {
		return this;
	}

	@Override
	public JSONPath type() {
		return new JSONArrayPath(parent, true, index + 1);
	}

	@Override
	public String toString() {
		return String.format("%s/%s",
				parent == null ? "" : parent.toString(),
				index == -1 ? "" : ((Integer) index).toString()
		);
	}
}
