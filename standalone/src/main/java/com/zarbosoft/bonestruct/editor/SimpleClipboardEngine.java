package com.zarbosoft.bonestruct.editor;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;

import java.nio.charset.StandardCharsets;

public class SimpleClipboardEngine extends ClipboardEngine {
	Clipboard clipboard = Clipboard.getSystemClipboard();
	DataFormat dataFormat = DataFormat.lookupMimeType("application/luxem");

	@Override
	public void set(final byte[] bytes) {
		final ClipboardContent content = new ClipboardContent();
		content.put(dataFormat, bytes);
		content.putString(new String(bytes, StandardCharsets.UTF_8));
		clipboard.setContent(content);
	}

	@Override
	public void setString(final String string) {
		final ClipboardContent content = new ClipboardContent();
		content.putString(string);
		clipboard.setContent(content);
	}

	@Override
	public byte[] get() {
		byte[] out = (byte[]) clipboard.getContent(dataFormat);
		if (out == null) {
			final String temp = clipboard.getString();
			if (temp != null) {
				out = temp.getBytes(StandardCharsets.UTF_8);
			}
		}
		return out;
	}

	@Override
	public String getString() {
		return clipboard.getString();
	}
}
