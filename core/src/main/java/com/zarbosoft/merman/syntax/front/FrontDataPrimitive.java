package com.zarbosoft.merman.syntax.front;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.FreeTag;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitive;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "primitive")
public class FrontDataPrimitive extends FrontPart {
	@Configuration
	private Set<String> tags = new HashSet<>();

	@Configuration
	public String middle;

	private MiddlePrimitive dataType;

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
		return new VisualPrimitive(
				context,
				parent,
				dataType.get(atom.data),
				HashTreePSet
						.from(tags)
						.plus(new PartTag("primitive"))
						.plusAll(this.tags.stream().map(s -> new FreeTag(s)).collect(Collectors.toSet())),
				visualDepth,
				depthScore
		);
	}

	public Set<String> tags() {
		return ImmutableSet.copyOf(tags);
	}

	public void tags(final Set<String> tags) {
		if (!this.tags.isEmpty())
			throw new AssertionError();
		this.tags = tags;
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
