package com.zarbosoft.merman.editor.visual.visuals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.last;

public class VisualGroup extends Visual {

	public VisualGroup(
			final Context context, final VisualParent parent, final int visualDepth, final int depthScore
	) {
		super(visualDepth);
		root(context, parent, parent.atomVisual().alignments(), visualDepth, depthScore);
	}

	protected VisualGroup(final int visualDepth) { /* Should only be called by inheritors... temp private */
		super(visualDepth);
	}

	@Override
	public void changeTags(final Context context, final TagsChange tagsChange) {
		children.forEach(child -> child.changeTags(context, tagsChange));
	}

	@Override
	public Stream<Brick> streamBricks() {
		return children.stream().flatMap(child -> child.streamBricks());
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		if (children.isEmpty())
			return null;
		return children.get(0).getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		if (children.isEmpty())
			return null;
		return last(children).getLastBrick(context);
	}

	@Override
	public boolean selectDown(final Context context) {
		for (final Visual child : children) {
			if (child.selectDown(context))
				return true;
		}
		return false;
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		if (children.isEmpty())
			throw new AssertionError();
		return children.get(0).createOrGetFirstBrick(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (children.isEmpty())
			return null;
		return children.get(0).createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		if (children.isEmpty())
			return null;
		return last(children).createLastBrick(context);
	}

	public VisualParent parent = null;

	// State
	public List<Visual> children = new ArrayList<>();

	@Override
	public VisualParent parent() {
		return parent;
	}

	public void add(final Context context, final Visual node, final int index) {
		if (index < 0)
			throw new AssertionError("Inserting visual atom at negative index.");
		if (index >= this.children.size() + 1)
			throw new AssertionError("Inserting visual atom after group end.");
		this.children.stream().skip(index).forEach(n -> ((Parent) n.parent()).index += 1);
		this.children.add(index, node);
		final Brick previousBrick =
				index == 0 ? parent.getPreviousBrick(context) : children.get(index - 1).getLastBrick(context);
		final Brick nextBrick = index + 1 >= this.children.size() ?
				parent.getNextBrick(context) :
				children.get(index + 1).getFirstBrick(context);
		if (previousBrick != null && nextBrick != null)
			context.idleLayBricksAfterEnd(previousBrick);
	}

	protected VisualParent createParent(final int index) {
		return new Parent(this, index);
	}

	public void add(final Context context, final Visual node) {
		add(context, node, children.size());
	}

	public void remove(final Context context, final int index) {
		if (index < 0)
			throw new AssertionError("Removing visual atom at negative index.");
		if (index >= this.children.size())
			throw new AssertionError("Removing visual atom after group end.");
		final Visual node = children.get(index);
		node.uproot(context, null);
		this.children.remove(index);
		this.children.stream().skip(index).forEach(n -> ((Parent) n.parent()).index -= 1);
	}

	public void remove(final Context context, final int start, final int size) {
		for (int index = start + size - 1; index >= start; --index) {
			remove(context, index);
		}
	}

	public void removeAll(final Context context) {
		remove(context, 0, children.size());
	}

	@Override
	public Iterator<Visual> children() {
		return Iterators.concat(children
				.stream()
				.map(c -> c.children())
				.toArray((IntFunction<Iterator<Visual>[]>) Iterator[]::new));
	}

	@Override
	public void compact(final Context context) {
		for (final Visual child : children)
			child.compact(context);
	}

	@Override
	public void expand(final Context context) {
		for (final Visual child : children)
			child.expand(context);
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

	@Override
	public void root(
			final Context context,
			final VisualParent parent,
			final Map<String, Alignment> alignments,
			final int depth,
			final int depthScore
	) {
		super.root(context, parent, alignments, depth, depthScore);
		this.parent = parent;
		for (int index = 0; index < children.size(); ++index) {
			final Visual child = children.get(index);
			child.root(context, child.parent(), alignments, depth + 1, depthScore);
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		for (final Visual child : Lists.reverse(children))
			child.uproot(context, root);
	}

	public static class Parent extends VisualParent {
		public final VisualGroup target;
		public int index;

		public Parent(final VisualGroup target, final int index) {
			this.target = target;
			this.index = index;
		}

		@Override
		public Brick createNextBrick(final Context context) {
			if (index + 1 < target.children.size())
				return target.children.get(index + 1).createFirstBrick(context);
			return target.parent.createNextBrick(context);
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			if (index - 1 >= 0)
				return target.children.get(index - 1).createLastBrick(context);
			return target.parent.createPreviousBrick(context);
		}

		@Override
		public Visual visual() {
			return target;
		}

		@Override
		public VisualAtom atomVisual() {
			return target.parent.atomVisual();
		}

		@Override
		public Brick findPreviousBrick(final Context context) {
			for (int at = index - 1; at >= 0; --at) {
				final Brick test = target.children.get(at).getLastBrick(context);
				if (test != null)
					return test;
			}
			return target.parent.findPreviousBrick(context);
		}

		@Override
		public Brick findNextBrick(final Context context) {
			for (int at = index + 1; at < target.children.size(); ++at) {
				final Brick test = target.children.get(at).getLastBrick(context);
				if (test != null)
					return test;
			}
			return target.parent.findNextBrick(context);
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			if (index == 0)
				return target.parent.getPreviousBrick(context);
			else
				return target.children.get(index - 1).getLastBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			if (index + 1 >= target.children.size())
				return target.parent.getNextBrick(context);
			else
				return target.children.get(index + 1).getFirstBrick(context);
		}

		@Override
		public Hoverable hover(
				final Context context, final com.zarbosoft.merman.editor.visual.Vector point
		) {
			return target.hover(context, point);
		}

		@Override
		public boolean selectNext(final Context context) {
			int test = index;
			while (++test < target.children.size()) {
				if (target.children.get(test).selectDown(context))
					return true;
			}
			return target.parent.selectNext(context);
		}

		@Override
		public boolean selectPrevious(final Context context) {
			int test = index;
			while (--test >= 0) {
				if (target.children.get(test).selectDown(context))
					return true;
			}
			return target.parent.selectPrevious(context);
		}

		public int getIndex() {
			return index;
		}
	}
}
