package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.visual.VisualNode;
import com.zarbosoft.luxemj.Luxem;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "primitive")
public class FrontDataPrimitive implements FrontPart {

	@Luxem.Configuration
	public String key;
	private DataPrimitive dataType;

	@Override
	public VisualNode createVisual(final Map<String, Object> data) {
		return new VisualNode() {
			Text text = new Text(dataType.get(data));

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

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		this.dataType = nodeType.getDataPrimitive(key);
	}
}
