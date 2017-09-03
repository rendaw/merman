package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.middle.MiddleArrayBase;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.merman.syntax.symbol.SymbolText;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class FrontDataArrayBase extends FrontPart {

	@Configuration(optional = true)
	public List<FrontSymbol> prefix = new ArrayList<>();
	@Configuration(optional = true)
	public List<FrontSymbol> suffix = new ArrayList<>();
	@Configuration(optional = true)
	public List<FrontSymbol> separator = new ArrayList<>();
	@Configuration(name = "tag_first", optional = true)
	public boolean tagFirst = false;
	@Configuration(name = "tag_last", optional = true)
	public boolean tagLast = false;

	@Configuration(optional = true)
	public Symbol ellipsis = new SymbolText("...");

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
	public Visual createVisual(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		return new VisualArray(
				context,
				parent,
				dataType.get(atom.data),
				tags
						.plus(new PartTag("array"))
						.plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet())),
				alignments,
				visualDepth,
				depthScore
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
