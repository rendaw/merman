package com.zarbosoft.bonestruct.editor.visual.nodes.parts;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;
import java.util.Set;

public abstract class VisualNodePart extends VisualNode {
	public VisualNodePart(final Set<Tag> tags) {
		super(tags);
	}

	@Override
	public int spacePriority() {
		throw new NotImplementedException();
	}

	@Override
	public boolean canCompact() {
		throw new NotImplementedException();
	}

	@Override
	public boolean canExpand() {
		throw new NotImplementedException();
	}

	@Override
	public void compact(final Context context) {
		changeTags(
				context,
				new TagsChange(ImmutableSet.of(new StateTag("compact")), ImmutableSet.of(new StateTag("expanded")))
		);
	}

	@Override
	public void expand(final Context context) {
		changeTags(
				context,
				new TagsChange(ImmutableSet.of(new StateTag("expanded")), ImmutableSet.of(new StateTag("compact")))
		);
	}

	@Override
	public void rootAlignments(final Context context, final Map<String, Alignment> alignments) {
	}

	public Context.Hoverable hover(final Context context) {
		return parent().hover(context);
	}
}
