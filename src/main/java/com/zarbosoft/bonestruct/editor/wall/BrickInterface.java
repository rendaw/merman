package com.zarbosoft.bonestruct.editor.wall;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.syntax.style.Style;

import java.util.Set;

public interface BrickInterface {

	VisualPart getVisual();

	/**
	 * @param context
	 * @return A new brick or null (no elements before or brick already exists)
	 */
	Brick createPrevious(Context context);

	/**
	 * @param context
	 * @return A new brick or null (no elements afterward or brick already exists)
	 */
	Brick createNext(Context context);

	void destroyed(Context context);

	Alignment getAlignment(Style.Baked style);

	Set<Visual.Tag> getTags(Context context);
}
