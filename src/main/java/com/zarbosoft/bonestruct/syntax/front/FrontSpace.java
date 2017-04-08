package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualSpace;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "space")
public class FrontSpace extends FrontConstantPart {
	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Set<VisualNode.Tag> tags) {
		return new VisualSpace(HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet())));
	}

}
