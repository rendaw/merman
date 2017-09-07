package com.zarbosoft.merman.editor.display.javafx;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.zarbosoft.merman.editor.display.Font;
import com.zarbosoft.rendaw.common.Pair;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

import java.util.concurrent.TimeUnit;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JavaFXFont implements Font {
	javafx.scene.text.Font font;

	static final Text helper = new Text();
	static final double DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
	static final double DEFAULT_LINE_SPACING = helper.getLineSpacing();
	static final String DEFAULT_TEXT = helper.getText();
	static final TextBoundsType DEFAULT_BOUNDS_TYPE = helper.getBoundsType();

	protected class Properties {
		final int ascent;
		final int descent;

		Properties() {
			helper.setFont(font);
			helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
			final double ascent1 = helper.getBaselineOffset();
			ascent = (int) ascent1;
			final Bounds bounds = helper.getLayoutBounds();
			final double height = bounds.getMaxY() - bounds.getMinY();
			// RESTORE STATE
			helper.setBoundsType(DEFAULT_BOUNDS_TYPE);
			descent = (int) (height - ascent);
		}
	}

	protected Properties properties = null;

	protected void properties() {
		if (properties == null)
			properties = new Properties();
	}

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
		properties();
		return properties.ascent;
	}

	@Override
	public int getDescent() {
		properties();
		return properties.descent;
	}

	private static final LoadingCache<Pair<JavaFXFont, String>, Integer> widthCache = CacheBuilder
			.newBuilder()
			.maximumSize(10_000)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build(new CacheLoader<Pair<JavaFXFont, String>, Integer>() {
				@Override
				public Integer load(final Pair<JavaFXFont, String> key) throws Exception {
					return key.first.internalGetWidth(key.second);
				}
			});

	public int internalGetWidth(final String text) {
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
	public int getWidth(final String text) {
		return uncheck(() -> widthCache.get(new Pair<>(this, text)));
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
