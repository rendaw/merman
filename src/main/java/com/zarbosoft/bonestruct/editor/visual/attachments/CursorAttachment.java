package com.zarbosoft.bonestruct.editor.visual.attachments;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.Drawing;
import com.zarbosoft.bonestruct.editor.display.Font;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.wall.Attachment;
import com.zarbosoft.bonestruct.editor.wall.bricks.BrickText;
import com.zarbosoft.bonestruct.syntax.style.ObboxStyle;

public class CursorAttachment {
	Drawing drawing;
	private ObboxStyle.Baked style;
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
		drawing.setPosition(
				context,
				new Vector(startConverse + brick.getConverseOffset(index), startTransverse + transverseAscent).add(
						offset),
				false
		);
	}

	Vector offset;
	private int startConverse;
	private int startTransverse;
	private int transverseAscent;
	private BrickText brick;

	public CursorAttachment(final Context context) {
		drawing = context.display.drawing();
		context.overlay.add(drawing);
	}

	public void setPosition(final Context context, final BrickText brick, final int index) {
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
		final Font font = brick.getFont();
		final int ascent = (int) (font.getAscent() * 1.8);
		final int descent = (int) (font.getDescent() * 1.8);
		final int halfBuffer = (int) (style.lineThickness / 2 + 0.5);
		final int buffer = halfBuffer * 2;
		final Vector size = new Vector(buffer + 1, ascent + (style.roundStart ? buffer : 0));
		drawing.clear();
		drawing.resize(context, size);
		final Drawing.DrawingContext gc = drawing.begin(context);
		gc.setLineThickness(style.lineThickness);
		if (style.roundStart)
			gc.setLineCapRound();
		else
			gc.setLineCapFlat();
		gc.setLineColor(style.lineColor);
		gc.beginPath();
		gc.moveTo(halfBuffer, halfBuffer);
		gc.lineTo(size.converse - halfBuffer - 1, size.transverse - halfBuffer - 1);
		gc.closePath();
		gc.stroke();
		/*
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
		*/
		offset = new Vector(halfBuffer, -ascent + descent - (style.roundStart ? halfBuffer : 0));
	}

	public void destroy(final Context context) {
		if (brick != null)
			brick.removeAttachment(context, this.attachment);
		context.overlay.remove(drawing);
	}

	public void setStyle(final Context context, final ObboxStyle.Baked style) {
		this.style = style;
		if (brick != null)
			redraw(context);
	}
}
