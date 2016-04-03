package com.zarbosoft.bonestruct.ui;

import java.util.ArrayList;
import java.util.List;

import com.zarbosoft.bonestruct.Helper;
import com.zarbosoft.bonestruct.model.Node;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Region;

public class UILeaf extends Region implements UINode {
	
	private static double glyphHeight = 16;
	private static double glyphWidth = 16;

	boolean split = false;
	class Line {
		String text;
		boolean newline;
		Canvas canvas;
		
		Line(String source) {
			this.text = source;
			newline = true;
			canvas = new Canvas(glyphWidth * length(), glyphHeight);
		}

		private double length() {
			int out = 0;
			for (int i = 0; i < text.length(); ++i) {
				int type = Character.getType(text.charAt(i));
				if (
					type == Character.NON_SPACING_MARK ||
					type == Character.ENCLOSING_MARK ||
					type == Character.COMBINING_SPACING_MARK)
					continue;
				++out;
			}
			return out;
		}
	}
	private List<Line> lines = new ArrayList<>();
	private double maxWidth = 0;
	
	public UILeaf(Node node) {
		this.node = node;
		Helper.stream(node.getText().split("\n"))
			.forEach(text -> {
				Line line = new Line(line);
				maxWidth = Math.max(maxWidth, line.getWidth());
				lines.append(line);
			});
		
	}

	@Override
	public Point2D end() {
		if (isSplit())
			return new Point2D(0, lines.size() * 16);
		return new Point2D(
			Helper.last(lines).canvas.getWidth(),
			(lines.size() - 1) * 16
		);
	}

	@Override
	public void split(double available) {
		for ()
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unsplit() {
		
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isSplit() {
		return split;
	}

	@Override
	public Point2D size() {
		return new Point2D(
			maxWidth, 
			values.size() * 16
		);
	}
}
