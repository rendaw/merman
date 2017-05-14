package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualSymbol;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.Walk;
import org.pcollections.PSet;

import java.util.Map;
import java.util.stream.Collectors;

@Configuration(name = "symbol")
public class FrontSymbol extends FrontPart {

	@Configuration
	public Symbol type;

	@Configuration(optional = true)
	public ConditionType condition = null;
	@Configuration(name = "gap_key", optional = true, description =
			"Use this string as a key for matching the atom when filling a gap. A text symbol " +
					"by default uses the shown text.")
	public String gapKey = "";

	@Override
	public VisualPart createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Visual.Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new VisualSymbol(
				context,
				parent,
				this,
				getTags(context, tags),
				atom == null ? null : condition == null ? null : condition.create(context, atom)
		);
	}

	public PSet<Visual.Tag> getTags(final Context context, final PSet<Visual.Tag> tags) {
		return tags
				.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
				.plus(new Visual.PartTag(Walk.decideName(type.getClass())));
	}

	public VisualPart createVisual(
			final Context context,
			final VisualParent parent,
			final PSet<Visual.Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return createVisual(context, parent, null, tags, alignments, depth);
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
