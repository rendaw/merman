package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.bricks.TextBrick;
import com.zarbosoft.bonestruct.editor.visual.raw.RawTextUtils;
import com.zarbosoft.bonestruct.editor.visual.wall.Attachment;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.StrokeLineCap;

public class CursorAttachment extends Canvas {
	private final ObboxStyle.Baked style;
	private final Attachment attachment = new Attachment() {
		@Override
		public void setTransverse(final Context context, final int transverse) {
			startTransverse = transverse;
			place(context);
		}

		@Override
		public void setConverse(final Context context, final int converse) {
			startConverse = converse;
			place(context);
		}

		@Override
		public void setTransverseSpan(final Context context, final int ascent, final int descent) {
			transverseAscent = ascent;
			place(context);
		}

		@Override
		public void destroy(final Context context) {
			brick = null;
		}
	};
	private int index;

	private void place(final Context context) {
		if (offset == null)
			return;
		final Point2D screen = context.toScreen(new Vector(startConverse + brick.getConverseOffset(index),
				startTransverse + transverseAscent
		));
		setLayoutX(screen.getX() + offset.getX());
		setLayoutY(screen.getY() + offset.getY());
	}

	Point2D offset;
	private int startConverse;
	private int startTransverse;
	private int transverseAscent;
	private TextBrick brick;

	public CursorAttachment(final Context context, final ObboxStyle.Baked style) {
		this.style = style;
		context.display.background.getChildren().add(this);
	}

	public void setPosition(final Context context, final TextBrick brick, final int index) {
		if (this.brick != brick) {
			offset = null;
			if (this.brick != null)
				this.brick.removeAttachment(context, this.attachment);

			this.brick = brick;
			if (this.brick == null)
				return;
			this.brick.addAttachment(context, this.attachment);
			redraw(context);
		}
		this.index = index;
		place(context);
	}

	private void redraw(final Context context) {
		final int ascent = (int) (RawTextUtils.getAscent(brick.getFont()) * 1.8);
		final int descent = (int) (RawTextUtils.getDescent(brick.getFont()) * 1.8);
		final int halfBuffer = (int) (style.lineThickness / 2 + 0.5);
		final int buffer = halfBuffer * 2;
		final Point2D size = context.toScreenSpan(new Vector(buffer + 1, ascent + (style.roundStart ? buffer : 0)));
		final GraphicsContext gc = getGraphicsContext2D();
		gc.clearRect(0, 0, getWidth(), getHeight());
		setWidth(size.getX());
		setHeight(size.getY());
		gc.setLineWidth(style.lineThickness);
		gc.setLineCap(style.roundStart ? StrokeLineCap.ROUND : StrokeLineCap.BUTT);
		gc.setStroke(style.lineColor);
		gc.beginPath();
		gc.moveTo(halfBuffer, halfBuffer);
		gc.lineTo(size.getX() - halfBuffer - 1, size.getY() - halfBuffer - 1);
		gc.closePath();
		gc.stroke();
		switch (context.syntax.converseDirection) {
			case UP:
				switch (context.syntax.transverseDirection) {
					case LEFT:
						offset = new Point2D(-ascent - (style.roundStart ? halfBuffer : 0), -(halfBuffer + 1));
						break;
					case RIGHT:
						offset = new Point2D(-descent - (style.roundStart ? halfBuffer : 0), -(halfBuffer + 1));
						break;
				}
				break;
			case DOWN:
				switch (context.syntax.transverseDirection) {
					case LEFT:
						offset = new Point2D(-ascent - (style.roundStart ? halfBuffer : 0), -(halfBuffer));
						break;
					case RIGHT:
						offset = new Point2D(-descent - (style.roundStart ? halfBuffer : 0), -(halfBuffer));
						break;
				}
				break;
			case LEFT:
				offset = new Point2D(-(halfBuffer + 1), -ascent + descent - (style.roundStart ? halfBuffer : 0));
				break;
			case RIGHT:
				offset = new Point2D(halfBuffer, -ascent + descent - (style.roundStart ? halfBuffer : 0));
				break;
		}
	}

	public void destroy(final Context context) {
		if (brick != null)
			brick.removeAttachment(context, this.attachment);
		context.display.background.getChildren().remove(this);
	}
}
