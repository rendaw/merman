package com.zarbosoft.merman.editor.display;

import com.zarbosoft.merman.editor.Context;

public class MockeryBlank extends MockeryDisplayNode implements Blank {
	private int converse;
	private int transverse;

	@Override
	public int converseSpan(final Context context) {
		return converse;
	}

	@Override
	public int transverseSpan(final Context context) {
		return transverse;
	}

	@Override
	public void setConverseSpan(final Context context, final int converse) {
		this.converse = converse;
	}

	@Override
	public void setTransverseSpan(final Context context, final int transverse) {
		this.transverse = transverse;
	}
}
