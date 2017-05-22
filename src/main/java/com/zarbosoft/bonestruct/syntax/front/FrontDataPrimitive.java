package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePrimitive;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "primitive")
public class FrontDataPrimitive extends FrontPart {

	@Configuration
	public String middle;

	private MiddlePrimitive dataType;

	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Override
	public VisualPart createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new VisualPrimitive(
				context,
				parent,
				dataType.get(atom.data),
				HashTreePSet
						.from(tags)
						.plus(new PartTag("primitive"))
						.plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet()))
		);
	}

	@Override
	public void finish(final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		this.dataType = atomType.getDataPrimitive(middle);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public String middle() {
		return middle;
	}
}
