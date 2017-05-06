package com.zarbosoft.bonestruct.editor.display;

import com.zarbosoft.bonestruct.editor.Context;

public class MockeryBlank extends MockeryDisplayNode implements Blank {
	@Override
	public int converseSpan(final Context context) {
		return 0;
	}

	@Override
	public int transverseSpan(final Context context) {
		return 0;
	}
}
