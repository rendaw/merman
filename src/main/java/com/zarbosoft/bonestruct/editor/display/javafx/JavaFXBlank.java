package com.zarbosoft.bonestruct.editor.display.javafx;

import com.zarbosoft.bonestruct.editor.display.Blank;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class JavaFXBlank extends JavaFXNode implements Blank {
	Region node = new Region();

	@Override
	protected Node node() {
		return node;
	}
}
