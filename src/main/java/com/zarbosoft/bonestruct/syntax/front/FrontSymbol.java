package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualSymbol;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.Walk;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.stream.Collectors;

@Configuration(name = "symbol")
public class FrontSymbol extends FrontPart {

	@Configuration
	public Symbol type;

	@Configuration(optional = true)
	public ConditionType condition = null;
	@Configuration(name = "gap_key", optional = true, description =
			"Use this string as a key for matching the node when filling a gap. A text symbol " +
					"by default uses the shown text.")
	public String gapKey = "";

	@Override
	public VisualPart createVisual(
			final Context context, final Node node, final PSet<Visual.Tag> tags
	) {
		return new VisualSymbol(
				this,
				getTags(context, tags),
				node == null ? null : condition == null ? null : condition.create(context, node)
		);
	}

	public PSet<Visual.Tag> getTags(final Context context, final PSet<Visual.Tag> tags) {
		return HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
				.plus(new Visual.PartTag(Walk.decideName(type.getClass())));
	}

	public VisualPart createVisual(final Context context, final PSet<Visual.Tag> tags) {
		return createVisual(context, null, tags);
	}

	@Override
	public String middle() {
		return null;
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	public DisplayNode createDisplay(final Context context) {
		final DisplayNode out = type.createDisplay(context);
		type.style(context, out, context.getStyle(getTags(context, context.globalTags)));
		return out;
	}
}
