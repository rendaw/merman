package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualMark;
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
	public VisualPart createVisual(final Context context, final Set<Visual.Tag> tags) {
		final VisualMark out = new VisualMark(
				this,
				HashTreePSet
						.from(tags)
						.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
						.plus(new Visual.PartTag("mark"))
		);
		out.setText(context, value);
		return out;
	}

}
