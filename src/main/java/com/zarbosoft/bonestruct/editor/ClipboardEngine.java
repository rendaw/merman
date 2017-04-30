package com.zarbosoft.bonestruct.editor;

public abstract class ClipboardEngine {
	public abstract void set(byte[] bytes);

	public abstract void setString(String string);

	public abstract byte[] get();

	public abstract String getString();
}
