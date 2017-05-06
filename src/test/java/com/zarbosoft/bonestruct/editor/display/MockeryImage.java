package com.zarbosoft.bonestruct.editor.display;

import com.zarbosoft.bonestruct.editor.Context;

import java.nio.file.Path;

public class MockeryImage extends MockeryDisplayNode implements Image {
	@Override
	public void setImage(final Context context, final Path path) {

	}

	@Override
	public void rotate(final Context context, final double rotate) {

	}

	@Override
	public int converseSpan(final Context context) {
		return 25;
	}

	@Override
	public int transverseSpan(final Context context) {
		return 25;
	}
}
