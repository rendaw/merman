package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.*;

public class VisualAtom extends Visual {
	public final Atom atom;
	private VisualParent parent;
	public int depth = 0;
	public boolean compact = false;
	private final Map<String, Alignment> alignments = new HashMap<>();
	private final Map<String, Alignment> localAlignments = new HashMap<>();
	private final List<Visual> children = new ArrayList<>();
	private final List<Visual> selectable = new ArrayList<>();

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
		for (final Pair<Integer, FrontPart> pair : iterable(enumerate(Common.stream(atom.type.front())))) {
			final int index = pair.first;
			final FrontPart front = pair.second;
			final Visual visual = pair.second.createVisual(
					context,
					front.middle() == null ?
							new ChildParent(index) :
							new SelectableChildParent(index, selectable.size()),
					atom,
					atom.tags,
					alignments,
					this.depth
			);
			children.add(visual);
			if (front.middle() != null)
				selectable.add(visual);
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
		children.forEach(child -> child.globalTagsChanged(context));
	}

	@Override
	public void changeTags(final Context context, final TagsChange tagsChange) {
		atom.tags = tagsChange.apply(atom.tags);
		children.forEach(child -> child.changeTags(context, tagsChange));
	}

	@Override
	public boolean selectDown(final Context context) {
		if (selectable.isEmpty())
			return false;
		selectable.get(0).selectDown(context);
		return true;
	}

	@Override
	public Stream<Brick> streamBricks() {
		return children.stream().flatMap(child -> child.streamBricks());
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		return children.get(0).createOrGetFirstBrick(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		return children.get(0).createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		return last(children).createLastBrick(context);
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		return children.get(0).getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		return last(children).getLastBrick(context);
	}

	public int spacePriority() {
		return -atom.type.precedence();
	}

	@Override
	public void compact(final Context context) {
		children.forEach(c -> c.compact(context));
		compact = true;
	}

	@Override
	public void expand(final Context context) {
		children.forEach(c -> c.expand(context));
		compact = false;
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return Iterables.concat(children
				.stream()
				.map(c -> c.getLeafPropertiesForTagsChange(context, change))
				.toArray(Iterable[]::new));
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
		for (int index = 0; index < children.size(); ++index) {
			final Visual child = children.get(index);
			child.root(context, child.parent(), this.alignments, this.depth);
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (root == this)
			return;
		atom.visual = null;
		for (final Visual child : Lists.reverse(children))
			child.uproot(context, root);
	}

	public AtomType type() {
		return atom.type;
	}

	private class ChildParent extends VisualParent {
		private final int index;

		public ChildParent(final int index) {
			this.index = index;
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
		public Brick createNextBrick(final Context context) {
			if (index + 1 < children.size())
				return children.get(index + 1).createFirstBrick(context);
			if (parent == null)
				return null;
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			return parent.createNextBrick(context);
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			if (index - 1 >= 0)
				return children.get(index - 1).createLastBrick(context);
			if (parent == null)
				return null;
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			return parent.createPreviousBrick(context);
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			if (index == 0) {
				if (context.windowAtom == VisualAtom.this.atom)
					return null;
				if (parent == null)
					return null;
				return parent.getPreviousBrick(context);
			} else
				return children.get(index - 1).getLastBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			if (index + 1 >= children.size()) {
				if (context.windowAtom == VisualAtom.this.atom)
					return null;
				if (parent == null)
					return null;
				return parent.getNextBrick(context);
			} else
				return children.get(index + 1).getFirstBrick(context);
		}

		@Override
		public Hoverable hover(final Context context, final Vector point) {
			if (parent == null)
				return null;
			return parent.hover(context, point);
		}

		@Override
		public boolean selectPrevious(final Context context) {
			throw new DeadCode();
		}

		@Override
		public boolean selectNext(final Context context) {
			throw new DeadCode();
		}

	}

	private class SelectableChildParent extends ChildParent {
		private final int selectableIndex;

		public SelectableChildParent(final int index, final int selectableIndex) {
			super(index);
			this.selectableIndex = selectableIndex;
		}

		@Override
		public boolean selectNext(final Context context) {
			int at = selectableIndex;
			while (++at < selectable.size())
				if (selectable.get(at).selectDown(context))
					return true;
			return false;
		}

		@Override
		public boolean selectPrevious(final Context context) {
			int at = selectableIndex;
			while (--at >= 0)
				if (selectable.get(at).selectDown(context))
					return true;
			return false;
		}
	}
}
