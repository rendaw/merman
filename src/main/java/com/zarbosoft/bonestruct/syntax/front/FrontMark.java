package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualMark;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "mark")
public class FrontMark extends FrontConstantPart {

	@Configuration
	public String value;

	@Configuration(optional = true)
	public ConditionType condition = null;

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualPart createVisual(
			final Context context, final Node node, final Set<Visual.Tag> tags
	) {
		final VisualMark out = new VisualMark(
				this,
				HashTreePSet
						.from(tags)
						.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
						.plus(new Visual.PartTag("mark")),
				node == null ? null : condition == null ? null : condition.create(context, node)
		);
		out.setText(context, value);
		return out;
	}

	@Override
	public VisualPart createVisual(final Context context, final Set<Visual.Tag> tags) {
		return createVisual(context, null, tags);
	}

}
