package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FrontDataArrayBase extends FrontPart {

	@Configuration(optional = true)
	public List<FrontSymbol> prefix = new ArrayList<>();
	@Configuration(optional = true)
	public List<FrontSymbol> suffix = new ArrayList<>();
	@Configuration(optional = true)
	public List<FrontSymbol> separator = new ArrayList<>();
	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();
	@Configuration(name = "tag_first", optional = true)
	public boolean tagFirst = false;
	@Configuration(name = "tag_last", optional = true)
	public boolean tagLast = false;

	@Configuration(name = "ellipsize_threshold", optional = true,
			description = "Ellipsize this element if the nodes depth exceeds this threshold.")
	public int ellipsizeThreshold = Integer.MAX_VALUE;

	@Configuration(optional = true, description = "How to visualize the ellipsis.")
	public Symbol ellipsis;

	protected MiddleArrayBase dataType;

	@Override
	public void finish(final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle());
		((MiddleArrayBase) atomType.middle().get(middle())).front = this;
		dataType = atomType.getDataArray(middle());
	}

	public abstract String middle();

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualPart createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Visual.Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		return new VisualArray(
				context,
				parent,
				dataType.get(atom.data),
				HashTreePSet
						.from(tags)
						.plus(new Visual.PartTag("array"))
						.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet())),
				alignments,
				depth
		) {

			@Override
			protected boolean tagLast() {
				return tagLast;
			}

			@Override
			protected boolean tagFirst() {
				return tagFirst;
			}

			@Override
			public int ellipsizeThreshold() {
				return ellipsizeThreshold;
			}

			@Override
			protected Symbol ellipsis() {
				return ellipsis;
			}

			@Override
			protected List<FrontSymbol> getPrefix() {
				return prefix;
			}

			@Override
			protected List<FrontSymbol> getSuffix() {
				return suffix;
			}

			@Override
			protected List<FrontSymbol> getSeparator() {
				return separator;
			}
		};
	}
}
