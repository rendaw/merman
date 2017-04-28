package com.zarbosoft.bonestruct.display;

public class MockeryFont implements Font {
	int size;

	public MockeryFont(final int fontSize) {
		this.size = fontSize;
	}

	@Override
	public int getAscent() {
		return (size * 8) / 10;
	}

	@Override
	public int getDescent() {
		return (size * 2) / 10;
	}

	@Override
	public int getWidth(final String text) {
		return size * text.length();
	}
}
