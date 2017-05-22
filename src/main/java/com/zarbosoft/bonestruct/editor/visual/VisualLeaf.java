package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.tags.StateTag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;

public interface VisualLeaf {
	boolean canCompact();

	boolean canExpand();

	Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
			Context context, TagsChange change
	);

	void changeTags(Context context, TagsChange change);

	default void changeTagsCompact(final Context context) {
		changeTags(context, new TagsChange().add(new StateTag("compact")));
	}

	default void changeTagsExpand(final Context context) {
		changeTags(context, new TagsChange().remove(new StateTag("compact")));
	}
}
