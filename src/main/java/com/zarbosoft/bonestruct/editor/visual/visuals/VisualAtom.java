package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.iterable;

public class VisualAtom extends Visual {
	private final VisualGroup body;
	public final Atom atom;
	private VisualParent parent;
	public int depth = 0;
	public boolean compact = false;
	private final Map<String, Alignment> alignments = new HashMap<>();
	private final Map<String, Alignment> localAlignments = new HashMap<>();

	public VisualAtom(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		this.atom = atom;
		for (final Map.Entry<String, AlignmentDefinition> entry : atom.type.alignments().entrySet()) {
			final Alignment alignment = entry.getValue().create();
			localAlignments.put(entry.getKey(), alignment);
		}
		rootInner(context, parent, alignments, depth);
		body = new VisualGroup(context, new BodyParent(), this.depth);
		for (final Pair<Integer, FrontPart> pair : iterable(enumerate(Common.stream(atom.type.front())))) {
			final Visual visual = pair.second.createVisual(context,
					body.createParent(pair.first),
					atom,
					atom.tags,
					alignments,
					this.depth
			);
			body.add(context, visual);
		}
		atom.visual = this;
	}

	@Override
	public VisualParent parent() {
		return parent;
	}

	public Map<String, Alignment> alignments() {
		return alignments;
	}

	public Alignment getAlignment(final String alignment) {
		return alignments.get(alignment);
	}

	@Override
	public void globalTagsChanged(final Context context) {
		body.globalTagsChanged(context);
	}

	@Override
	public void changeTags(final Context context, final TagsChange tagsChange) {
		atom.tags = tagsChange.apply(atom.tags);
		body.changeTags(context, tagsChange);
	}

	@Override
	public boolean canCreateBricks(final Context context) {
		return body.canCreateBricks(context);
	}

	@Override
	public boolean selectDown(final Context context) {
		return body.selectDown(context);
	}

	@Override
	public Stream<Brick> streamBricks() {
		return body.streamBricks();
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		return body.createOrGetFirstBrick(context);
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

	public int spacePriority() {
		return -atom.type.precedence();
	}

	@Override
	public void compact(final Context context) {
		body.compact(context);
		compact = true;
	}

	@Override
	public void expand(final Context context) {
		body.expand(context);
		compact = false;
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return body.getLeafPropertiesForTagsChange(context, change);
	}

	private void rootInner(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		compact = false;
		this.parent = parent;
		if (parent == null)
			this.depth = 0;
		else
			this.depth = depth + atom.type.depthScore;
		this.alignments.clear();
		this.alignments.putAll(alignments);
		for (final Map.Entry<String, Alignment> alignment : localAlignments.entrySet()) {
			alignment.getValue().root(context, alignments);
			this.alignments.put(alignment.getKey(), alignment.getValue());
		}
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		rootInner(context, parent, alignments, depth);
		body.root(context, body.parent, this.alignments, this.depth);
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (root == this)
			return;
		atom.visual = null;
		body.uproot(context, root);
	}

	public AtomType type() {
		return atom.type;
	}

	private class BodyParent extends VisualParent {
		@Override
		public VisualParent parent() {
			return parent;
		}

		@Override
		public Brick createNextBrick(final Context context) {
			if (parent == null)
				return null;
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			return parent.createNextBrick(context);
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			if (parent == null)
				return null;
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			return parent.createPreviousBrick(context);
		}

		@Override
		public Visual visual() {
			return VisualAtom.this;
		}

		@Override
		public VisualAtom atomVisual() {
			return VisualAtom.this;
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			if (parent == null)
				return null;
			return parent.getPreviousBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			if (context.windowAtom == VisualAtom.this.atom)
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
	}
}
