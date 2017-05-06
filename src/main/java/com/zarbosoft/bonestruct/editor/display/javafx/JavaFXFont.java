package com.zarbosoft.bonestruct.editor.display.javafx;

import com.zarbosoft.bonestruct.editor.display.Font;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class JavaFXFont implements Font {
	javafx.scene.text.Font font;

	static final Text helper = new Text();
	static final double DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
	static final double DEFAULT_LINE_SPACING = helper.getLineSpacing();
	static final String DEFAULT_TEXT = helper.getText();
	static final TextBoundsType DEFAULT_BOUNDS_TYPE = helper.getBoundsType();

	public JavaFXFont(
			final String font, final int size
	) {
		if (font == null)
			this.font = javafx.scene.text.Font.font(size);
		else
			this.font = javafx.scene.text.Font.font(font, size);
	}

	public JavaFXFont(final javafx.scene.text.Font font) {
		this.font = font;
	}

	@Override
	public int getAscent() {
		helper.setFont(font);
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		final double ascent = helper.getBaselineOffset();
		// RESTORE STATE
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		return (int) ascent;
	}

	@Override
	public int getDescent() {
		helper.setFont(font);
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		final double ascent = helper.getBaselineOffset();
		final Bounds bounds = helper.getLayoutBounds();
		final double height = bounds.getMaxY() - bounds.getMinY();
		// RESTORE STATE
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		return (int) (height - ascent);
	}

	@Override
	public int getWidth(final String text) {
		helper.setText(text);
		helper.setFont(font);
		// Note that the wrapping width needs to be set to zero before
		// getting the text's real preferred width.
		helper.setWrappingWidth(0);
		helper.setLineSpacing(0);
		double w = helper.prefWidth(-1);
		helper.setWrappingWidth((int) Math.ceil(w));
		w = Math.ceil(helper.getLayoutBounds().getWidth());
		// RESTORE STATE
		helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
		helper.setLineSpacing(DEFAULT_LINE_SPACING);
		helper.setText(DEFAULT_TEXT);
		return (int) w;
	}

	@Override
	public int getUnder(final String text, final int converse) {
		helper.setText(text);
		helper.setFont(font);
		// Note that the wrapping width needs to be set to zero before
		// getting the text's real preferred width.
		helper.setWrappingWidth(0);
		helper.setLineSpacing(0);
		final double w = helper.prefWidth(-1);
		helper.setWrappingWidth((int) Math.ceil(w));
		final int offset = helper.hitTest(new Point2D(converse, 0)).getInsertionIndex();
		// RESTORE STATE
		helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
		helper.setLineSpacing(DEFAULT_LINE_SPACING);
		helper.setText(DEFAULT_TEXT);
		return offset;
	}
}
