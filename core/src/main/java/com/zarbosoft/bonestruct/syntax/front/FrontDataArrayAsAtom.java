package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNestedFromArray;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FrontDataArrayAsAtom extends FrontPart {

	@Override
	public String middle() {
		return middle;
	}

	@Configuration
	public String middle;
	private MiddleArray dataType;

	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Override
	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new VisualNestedFromArray(
				context,
				parent,
				dataType.get(atom.data),
				HashTreePSet
						.from(tags)
						.plus(new PartTag("nested"))
						.plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet())),
				alignments,
				depth
		) {

			@Override
			protected Symbol ellipsis() {
				return null;
			}
		};
	}

	@Override
	public void finish(final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = (MiddleArray) atomType.getDataArray(middle);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
	}
}
