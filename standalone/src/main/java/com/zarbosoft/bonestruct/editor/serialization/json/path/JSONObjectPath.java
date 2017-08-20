package com.zarbosoft.bonestruct.editor.serialization.json.path;

public class JSONObjectPath extends JSONPath {

	private String key;

	public JSONObjectPath(final JSONPath parent) {
		this.parent = parent;
	}

	public JSONObjectPath(final JSONPath parent, final String key) {
		this.parent = parent;
		this.key = key;
	}

	@Override
	public JSONPath value() {
		return this;
	}

	@Override
	public JSONPath key(final String data) {
		return new JSONObjectPath(parent, data);
	}

	@Override
	public JSONPath type() {
		return this;
	}

	@Override
	public String toString() {
		return String.format("%s/%s", parent == null ? "" : parent.toString(), key == null ? "" : key);
	}
}
