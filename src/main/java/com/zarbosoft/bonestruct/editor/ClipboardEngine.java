package com.zarbosoft.bonestruct.editor;

public abstract class ClipboardEngine {
	public abstract void set(byte[] bytes);

	public abstract byte[] get();
}
