package com.zarbosoft.bonestruct.editor.display.javafx;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.Blank;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class JavaFXBlank extends JavaFXNode implements Blank {
	Region node = new Region();

	@Override
	protected Node node() {
		return node;
	}

	@Override
	public void setConverseSpan(final Context context, final int converse) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				node.setMinHeight(converse);
				break;
			case LEFT:
			case RIGHT:
				node.setMinWidth(converse);
				break;
		}
	}

	@Override
	public void setTransverseSpan(final Context context, final int transverse) {
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				node.setMinHeight(transverse);
				break;
			case LEFT:
			case RIGHT:
				node.setMinWidth(transverse);
				break;
		}
	}
}
