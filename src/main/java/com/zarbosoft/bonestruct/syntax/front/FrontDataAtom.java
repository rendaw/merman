package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.FreeTag;
import com.zarbosoft.bonestruct.editor.visual.tags.PartTag;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNested;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddleAtom;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.bonestruct.syntax.symbol.SymbolText;
import com.zarbosoft.interface1.Configuration;
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

	@Configuration(optional = true, description = "How to visualize the ellipsis.")
	public Symbol ellipsis = new SymbolText("...");

	@Override
	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new VisualNested(
				context,
				parent,
				dataType.get(atom.data),
				tags
						.plus(new PartTag("nested"))
						.plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet())),
				alignments,
				depth
		) {

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
