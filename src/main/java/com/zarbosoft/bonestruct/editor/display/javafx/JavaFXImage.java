package com.zarbosoft.bonestruct.editor.display.javafx;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.Image;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

import java.nio.file.Path;

public class JavaFXImage extends JavaFXNode implements Image {
	protected final ImageView view = new ImageView();

	@Override
	public void setImage(final Context context, final Path path) {
		final Vector at = position(context);
		view.setImage(new javafx.scene.image.Image(path.toString()));
		setPosition(context, at, false);
	}

	@Override
	public void rotate(final Context context, final double rotate) {
		view.setRotate(rotate);
	}

	@Override
	protected Node node() {
		return view;
	}
}
