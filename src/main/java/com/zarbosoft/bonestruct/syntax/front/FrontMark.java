package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualMark;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "mark")
public class FrontMark extends FrontConstantPart {

	@Configuration
	public String value;

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Set<VisualNode.Tag> tags) {
		final VisualMark out = new VisualMark(
				this,
				HashTreePSet
						.from(tags)
						.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
						.plus(new VisualNode.PartTag("mark"))
		);
		out.setText(context, value);
		return out;
	}

}
