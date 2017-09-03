package com.zarbosoft.merman.editor.visual;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;

public interface VisualLeaf {
	Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
			Context context, TagsChange change
	);

	Hoverable hover(final Context context, final Vector point); // Should map to method in Visual

	VisualParent parent(); // Should map to method in Visual

	default VisualAtom atomVisual() {
		return parent().atomVisual();
	}
}
