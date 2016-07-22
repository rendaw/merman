package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataRecord;
import com.zarbosoft.bonestruct.visual.VisualNode;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

import java.util.*;

@Luxem.Configuration(name = "record")
public class FrontDataRecord implements FrontPart {

	@Luxem.Configuration
	public String key;
	@Luxem.Configuration
	public List<FrontConstantPart> prefix;
	@Luxem.Configuration
	public List<FrontConstantPart> infix;
	@Luxem.Configuration
	public List<FrontConstantPart> suffix;
	@Luxem.Configuration
	public List<FrontConstantPart> separator;
	private DataRecord dataType;

	@Override
	public VisualNode createVisual(final Map<String, Object> data) {
		class RecordKeyVisual implements VisualNode {
			final Text text;

			public RecordKeyVisual(final Pair<String, Node> pair) {
				text = new Text(pair.first);
			}

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
		}
		class RecordElementVisual implements VisualNode {
			List<VisualNode> children = new ArrayList<>();
			Point2D end = new Point2D(0, 0);
			Pane pane = new Pane();

			public RecordElementVisual(final boolean first, final Pair<String, Node> pair) {
				for (final FrontConstantPart fix : prefix)
					add(fix.createVisual());
				add(new RecordKeyVisual(pair));
				for (final FrontConstantPart fix : infix)
					add(fix.createVisual());
				add(pair.second.createVisual());
				for (final FrontConstantPart fix : suffix)
					add(fix.createVisual());
				if (first) {
					for (final FrontConstantPart fix : separator)
						add(fix.createVisual());
				}
			}

			private void add(final VisualNode node) {
				children.add(node);
				pane.getChildren().add(node.visual());
			}

			@Override
			public Point2D end() {
				return end;
			}

			@Override
			public javafx.scene.Node visual() {
				return pane;
			}

			@Override
			public void offset(final Point2D offset) {
				pane.setTranslateX(offset.getX());
				pane.setTranslateY(offset.getY());
			}

			@Override
			public Iterator<VisualNode> children() {
				return children.iterator();
			}

			@Override
			public void layoutInitial() {
				end = Point2D.ZERO;
				for (final VisualNode child : children) {
					child.offset(end);
					end = end.add(child.end());
				}
			}
		}
		class RecordVisual implements VisualNode {
			List<VisualNode> children = new ArrayList<>();
			Point2D end = new Point2D(0, 0);
			Pane pane = new Pane();

			public void add(final VisualNode node) {
				this.children.add(node);
				pane.getChildren().add(node.visual());
				end = end.add(node.end());
			}

			@Override
			public Point2D end() {
				return end;
			}

			@Override
			public javafx.scene.Node visual() {
				return pane;
			}

			@Override
			public Iterator<VisualNode> children() {
				return children.iterator();
			}

			@Override
			public void layoutInitial() {
				end = Point2D.ZERO;
				for (final VisualNode child : children) {
					child.offset(end);
					end = end.add(child.end());
				}
			}

			@Override
			public void offset(final Point2D offset) {
				pane.setTranslateX(offset.getX());
				pane.setTranslateY(offset.getY());
			}
		}
		final RecordVisual out = new RecordVisual();
		boolean first = true;
		for (final Pair<String, Node> pair : dataType.get(data)) {
			out.add(new RecordElementVisual(first, pair));
			first = false;
		}
		return out;
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		dataType = nodeType.getDataRecord(key);
	}
}
