package com.zarbosoft.bonestruct.visual;

import javafx.scene.Node;

public class Layer {
	public Node foreground;
	public Node background;

	public Layer(final Node foreground, final Node background) {
		this.foreground = foreground;
		this.background = background;
	}
}
