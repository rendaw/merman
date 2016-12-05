package com.zarbosoft.bonestruct.visual.nodes.parts;

import javafx.geometry.Bounds;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class RawTextUtils {
	static final Text helper = new Text();
	static final double DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
	static final double DEFAULT_LINE_SPACING = helper.getLineSpacing();
	static final String DEFAULT_TEXT = helper.getText();
	static final TextBoundsType DEFAULT_BOUNDS_TYPE = helper.getBoundsType();

	public static double getAscent(final Font font) {
		helper.setFont(font);
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		final double ascent = helper.getBaselineOffset();
		// RESTORE STATE
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		return ascent;
	}

	public static double getDescent(final Font font) {
		helper.setFont(font);
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		final double ascent = helper.getBaselineOffset();
		final Bounds bounds = helper.getLayoutBounds();
		final double height = bounds.getMaxY() - bounds.getMinY();
		// RESTORE STATE
		helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
		return height - ascent;
	}

	public static double computeTextWidth(final Font font, final String text) {
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
		return w;
	}
}
