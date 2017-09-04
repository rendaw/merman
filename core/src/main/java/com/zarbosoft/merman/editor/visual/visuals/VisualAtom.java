package com.zarbosoft.merman.editor.visual.visuals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Attachment;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.front.FrontPart;
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
	public int depthScore = 0;
	public boolean compact = false;
	private final Map<String, Alignment> alignments = new HashMap<>();
	private final Map<String, Alignment> localAlignments = new HashMap<>();
	private final List<Visual> children = new ArrayList<>();
	private final List<Visual> selectable = new ArrayList<>();
	public Brick firstBrick;
	Attachment firstBrickListener = new Attachment() {
		@Override
		public void destroy(final Context context) {
			firstBrick = null;
		}
	};
	public Brick lastBrick;
	Attachment lastBrickListener = new Attachment() {
		@Override
		public void destroy(final Context context) {
			lastBrick = null;
		}
	};

	public VisualAtom(
			final Context context,
			final VisualParent parent,
			final Atom atom,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		super(visualDepth);
		this.atom = atom;
		for (final Map.Entry<String, AlignmentDefinition> entry : atom.type.alignments().entrySet()) {
			final Alignment alignment = entry.getValue().create();
			localAlignments.put(entry.getKey(), alignment);
		}
		rootInner(context, parent, alignments, visualDepth, depthScore);
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
					this.visualDepth + 1,
					this.depthScore
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
			final Context context,
			final VisualParent parent,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		compact = false;
		this.parent = parent;
		if (parent == null) {
			this.visualDepth = 0;
			this.depthScore = 0;
		} else {
			this.visualDepth = visualDepth;
			this.depthScore = depthScore + atom.type.depthScore();
		}
		this.alignments.clear();
		this.alignments.putAll(alignments);
		for (final Map.Entry<String, Alignment> alignment : localAlignments.entrySet()) {
			alignment.getValue().root(context, alignments);
			this.alignments.put(alignment.getKey(), alignment.getValue());
		}
	}

	@Override
	public void root(
			final Context context,
			final VisualParent parent,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		rootInner(context, parent, alignments, visualDepth, depthScore);
		for (int index = 0; index < children.size(); ++index) {
			final Visual child = children.get(index);
			child.root(context, child.parent(), this.alignments, this.visualDepth + 1, this.depthScore);
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (root == this)
			return;
		atom.visual = null;
		for (final Visual child : Lists.reverse(children))
			child.uproot(context, root);
		for (final Map.Entry<String, Alignment> entry : localAlignments.entrySet())
			entry.getValue().destroy(context);
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
		public Brick findPreviousBrick(final Context context) {
			for (int at = index - 1; at >= 0; --at) {
				final Brick test = children.get(at).getLastBrick(context);
				if (test != null)
					return test;
			}
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			if (parent == null)
				return null;
			return parent.findPreviousBrick(context);
		}

		@Override
		public Brick findNextBrick(final Context context) {
			for (int at = index + 1; at < children.size(); ++at) {
				final Brick test = children.get(at).getLastBrick(context);
				if (test != null)
					return test;
			}
			if (context.windowAtom == VisualAtom.this.atom)
				return null;
			if (parent == null)
				return null;
			return parent.findNextBrick(context);
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

		@Override
		public void bricksCreated(final Context context, final ArrayList<Brick> bricks) {
			Brick min = firstBrick;
			Brick max = lastBrick;
			for (final Brick brick : bricks) {
				if (min == null ||
						brick.parent.index < min.parent.index ||
						brick.parent.index == min.parent.index && brick.index < min.index) {
					min = brick;
				}
				if (max == null ||
						brick.parent.index > max.parent.index ||
						brick.parent.index == max.parent.index && brick.index > max.index) {
					max = brick;
				}
			}
			final ArrayList<Brick> out = new ArrayList<>();
			if (min != firstBrick) {
				if (firstBrick == null)
					firstBrick.removeAttachment(context, firstBrickListener);
				firstBrick = min;
				firstBrick.addAttachment(context, firstBrickListener);
				if (parent != null)
					parent.firstBrickChanged(context, firstBrick);
				out.add(min);
			}
			if (max != lastBrick) {
				if (lastBrick == null)
					lastBrick.removeAttachment(context, lastBrickListener);
				lastBrick = max;
				lastBrick.addAttachment(context, lastBrickListener);
				if (parent != null)
					parent.lastBrickChanged(context, lastBrick);
				out.add(max);
			}
			context.bricksCreated(VisualAtom.this, out);
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
