package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddleAtom;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "atom")
public class FrontDataAtom extends FrontPart {

	@Override
	public String middle() {
		return middle;
	}

	@Configuration
	public String middle;
	private MiddleAtom dataType;

	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Configuration(name = "ellipsize_threshold", optional = true,
			description = "Ellipsize this element if the nodes depth exceeds this threshold.")
	public int ellipsizeThreshold = Integer.MAX_VALUE;

	@Configuration(optional = true, description = "How to visualize the ellipsis.")
	public Symbol ellipsis;

	@Override
	public VisualPart createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Visual.Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new VisualAtom(
				context,
				parent,
				dataType.get(atom.data),
				HashTreePSet
						.from(tags)
						.plus(new Visual.PartTag("nested"))
						.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet())),
				alignments,
				depth
		) {

			@Override
			public int ellipsizeThreshold() {
				return ellipsizeThreshold;
			}

			@Override
			protected Symbol ellipsis() {
				return ellipsis;
			}
		};
	}

	@Override
	public void finish(final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = atomType.getDataNode(middle);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}
}