package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualImage;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "image")
public class FrontImage extends FrontConstantPart {

	@Configuration(name = "gap-key", optional = true,
			description = "Use this string as a key for matching the node when filling a gap.")
	public String gapKey = "";

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Set<VisualNode.Tag> tags) {
		final VisualImage out = new VisualImage(HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
				.plus(new VisualNode.PartTag("image")));
		return out;
	}

}
