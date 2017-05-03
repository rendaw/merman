package com.zarbosoft.bonestruct.display;

public class MockeryFont implements Font {
	int size = 10;

	public MockeryFont(final int fontSize) {
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

	@Override
	public int getUnder(final String text, final int converse) {
		return Math.min(text.length(), converse / size);
	}
}
