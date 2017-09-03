package com.zarbosoft.merman.editor.display;

public interface Font {
	int getAscent();

	int getDescent();

	int getWidth(String text);

	int getUnder(String text, int converse);
}
