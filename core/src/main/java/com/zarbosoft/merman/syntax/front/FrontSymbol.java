package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.Walk;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualSymbol;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.Map;

@Configuration(name = "symbol")
public class FrontSymbol extends FrontPart {

	@Configuration
	public Symbol type;

	@Configuration(optional = true)
	public ConditionType condition = null;

	@Configuration(name = "gap_key", optional = true)
	public String gapKey = "";

	@Override
	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		return new VisualSymbol(
				context,
				parent,
				this,
				getTags(context, tags),
				atom == null ? null : condition == null ? null : condition.create(context, atom),
				visualDepth
		);
	}

	public PSet<Tag> getTags(final Context context, final PSet<Tag> tags) {
		return tags.plusAll(Context.asFreeTags(this.tags)).plus(new PartTag(Walk.decideName(type.getClass())));
	}

	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		return createVisual(context, parent, null, tags, alignments, visualDepth, depthScore);
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
		// TODO should include the AtomType id or atom's tags
		type.style(context, out, context.getStyle(context.globalTags.plusAll(getTags(context, HashTreePSet.empty()))));
		return out;
	}
}
