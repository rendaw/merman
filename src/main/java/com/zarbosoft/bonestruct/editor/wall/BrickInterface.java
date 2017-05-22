package com.zarbosoft.bonestruct.editor.wall;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.VisualLeaf;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.syntax.style.Style;
import org.pcollections.PSet;

public interface BrickInterface {

	VisualLeaf getVisual();

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

	void brickDestroyed(Context context);

	Alignment getAlignment(Style.Baked style);

	PSet<Tag> getTags(Context context);
}
