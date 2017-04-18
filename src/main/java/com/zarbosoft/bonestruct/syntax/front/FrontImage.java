package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualImage;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "image")
public class FrontImage extends FrontConstantPart {

	@Configuration(name = "gap_key", optional = true,
			description = "Use this string as a key for matching the node when filling a gap.")
	public String gapKey = "";

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualPart createVisual(final Context context, final Set<Visual.Tag> tags) {
		final VisualImage out = new VisualImage(HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
				.plus(new Visual.PartTag("image")));
		return out;
	}

}
