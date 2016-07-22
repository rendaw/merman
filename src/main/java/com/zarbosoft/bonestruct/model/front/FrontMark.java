package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.visual.VisualNode;
import com.zarbosoft.luxemj.Luxem;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;

@Luxem.Configuration(name = "mark")
public class FrontMark implements FrontConstantPart {

	@Luxem.Configuration
	public String value;

	@Override
	public VisualNode createVisual() {
		return new VisualNode() {
			Text text = new Text(value);

			@Override
			public Point2D end() {
				return new Point2D(text.getLayoutBounds().getWidth(), 0);
			}

			@Override
			public javafx.scene.Node visual() {
				return text;
			}

			@Override
			public void offset(final Point2D offset) {
				text.setTranslateX(offset.getX());
				text.setTranslateY(offset.getY());
			}

		};
	}
}
