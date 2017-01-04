package com.zarbosoft.bonestruct.editor.visual;

import javafx.scene.Node;

public abstract class Fixture {
	public Course parent;
	public int index;

	public abstract int transverseSpan(Context context);

	public abstract Node visual(Context context);
}
