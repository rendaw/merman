package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashMap;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.enumerate;

public class VisualAtomType extends Visual {
	private final AtomType atomType;
	private final VisualGroup body;
	public final Atom atom;
	private boolean compact;
	private VisualParent parent;
	public int depth = 0;

	public VisualAtomType(
			final Context context,
			final VisualParent parent,
			final AtomType atomType,
			final Atom atom,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		super(HashTreePSet.<Tag>empty().plus(new TypeTag(atomType.id)).plus(new PartTag("atom")));
		this.atomType = atomType;
		this.atom = atom;
		final PSet<Tag> tags = HashTreePSet.singleton(new TypeTag(atomType.id));
		compact = false;
		final Map<String, Alignment> bodyAlignments = new HashMap<>(alignments);
		for (final Map.Entry<String, AlignmentDefinition> entry : atomType.alignments().entrySet()) {
			bodyAlignments.put(entry.getKey(), entry.getValue().create());
		}
		body = new VisualGroup(context, new VisualParent() {
			@Override
			public VisualParent parent() {
				return parent;
			}

			@Override
			public Brick createNextBrick(final Context context) {
				if (context.windowAtom == VisualAtomType.this.atom)
					return null;
				return parent.createNextBrick(context);
			}

			@Override
			public Brick createPreviousBrick(final Context context) {
				if (context.windowAtom == VisualAtomType.this.atom)
					return null;
				return parent.createPreviousBrick(context);
			}

			@Override
			public Visual getTarget() {
				return parent.getTarget();
			}

			@Override
			public VisualAtomType getNodeVisual() {
				return VisualAtomType.this;
			}

			@Override
			public Alignment getAlignment(final String alignment) {
				return parent.getAlignment(alignment);
			}

			@Override
			public Brick getPreviousBrick(final Context context) {
				if (context.windowAtom == VisualAtomType.this.atom)
					return null;
				if (parent == null)
					return null;
				return parent.getPreviousBrick(context);
			}

			@Override
			public Brick getNextBrick(final Context context) {
				if (context.windowAtom == VisualAtomType.this.atom)
					return null;
				if (parent == null)
					return null;
				return parent.getNextBrick(context);
			}

			@Override
			public Hoverable hover(
					final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector point
			) {
				if (parent == null)
					return null;
				return parent.hover(context, point);
			}
		}, HashTreePSet.empty(), bodyAlignments, depth + atomType.depthScore);
		enumerate(Common.stream(atomType.front())).forEach(pair -> {
			final VisualPart visual =
					pair.second.createVisual(context, body.createParent(pair.first), atom, tags, alignments, depth);
			body.add(context, visual);
		});
		atom.visual = this;
	}

	@Override
	public VisualParent parent() {
		return body.parent();
	}

	@Override
	public boolean selectDown(final Context context) {
		return body.selectDown(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		return body.createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		return body.createLastBrick(context);
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		return body.getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		return body.getLastBrick(context);
	}

	@Override
	public int spacePriority() {
		return -atomType.precedence();
	}

	@Override
	public boolean canCompact() {
		return !compact;
	}

	@Override
	public void compact(final Context context) {
		body.compact(context);
		compact = true;
	}

	@Override
	public boolean canExpand() {
		return compact;
	}

	@Override
	public void expand(final Context context) {
		body.expand(context);
		compact = false;
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return body.getPropertiesForTagsChange(context, change);
	}

	private void rootInner(final Context context, final int depth) {
		this.depth = depth;
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		this.parent = parent;
		rootInner(context, depth);
		body.root(context, body.parent, alignments, depth + atomType.depthScore);
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (root == this)
			return;
		atom.visual = null;
		body.uproot(context, root);
	}

	@Override
	public void tagsChanged(final Context context) {

	}

	public AtomType getType() {
		return atomType;
	}
}
