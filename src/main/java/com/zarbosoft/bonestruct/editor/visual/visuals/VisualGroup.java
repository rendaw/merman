package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;
import org.pcollections.PSet;

import java.util.*;
import java.util.function.IntFunction;

import static com.zarbosoft.rendaw.common.Common.last;

public class VisualGroup extends VisualPart {

	public VisualGroup(
			final Context context,
			final VisualParent parent,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		super(tags);
		root(context, parent, alignments, depth);
	}

	protected VisualGroup(final PSet<Tag> tags) { // Should only be called by inheritors... temp private
		super(tags);
	}

	@Override
	public Map<String, Alignment> alignments() {
		return alignments;
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
		for (final VisualPart child : children) {
			if (child.selectDown(context))
				return true;
		}
		return false;
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
		return last(children).createFirstBrick(context);
	}

	public Map<String, Alignment> alignments = new HashMap<>();
	public VisualParent parent = null;

	// State
	IdleTask idle;
	protected List<VisualPart> children = new ArrayList<>();
	boolean compact = false;

	@Override
	public VisualParent parent() {
		return parent;
	}

	public void add(final Context context, final VisualPart node, final int index) {
		if (index < 0)
			throw new AssertionError("Inserting visual atom at negative index.");
		if (index >= this.children.size() + 1)
			throw new AssertionError("Inserting visual atom after group end.");
		this.children.stream().skip(index).forEach(n -> ((Parent) n.parent()).index += 1);
		this.children.add(index, node);
		final Brick previousBrick = index == 0 ?
				(parent == null ? null : parent.getPreviousBrick(context)) :
				children.get(index - 1).getLastBrick(context);
		final Brick nextBrick = index + 1 >= this.children.size() ?
				(parent == null ? null : parent.getNextBrick(context)) :
				children.get(index + 1).getFirstBrick(context);
		if (previousBrick != null && nextBrick != null)
			context.idleLayBricksAfterEnd(previousBrick);
	}

	protected VisualParent createParent(final int index) {
		return new Parent(this, index);
	}

	public void add(final Context context, final VisualPart node) {
		add(context, node, children.size());
	}

	public void remove(final Context context, final int index) {
		if (index < 0)
			throw new AssertionError("Removing visual atom at negative index.");
		if (index >= this.children.size())
			throw new AssertionError("Removing visual atom after group end.");
		final VisualPart node = children.get(index);
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
		super.compact(context);
		children.forEach(c -> c.compact(context));
	}

	@Override
	public void expand(final Context context) {
		super.expand(context);
		children.forEach(c -> c.expand(context));
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return Iterables.concat(children
				.stream()
				.map(c -> c.getPropertiesForTagsChange(context, change))
				.toArray(Iterable[]::new));
	}

	@Override
	public Alignment getAlignment(final String alignment) {
		final Alignment out = alignments.get(alignment);
		if (out != null)
			return out;
		return super.getAlignment(alignment);
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		this.parent = parent;
		PMap<String, Alignment> derived = HashTreePMap.from(alignments);
		for (final Map.Entry<String, Alignment> e : this.alignments.entrySet()) {
			final Alignment alignment = e.getValue();
			alignment.root(context, alignments);
			derived = derived.plus(e.getKey(), alignment);
		}
		for (int index = 0; index < children.size(); ++index) {
			final VisualPart child = children.get(index);
			child.root(context, child.parent(), derived, depth);
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		for (final VisualPart child : children)
			child.uproot(context, root);
	}

	@Override
	public void tagsChanged(final Context context) {

	}

	public static class Parent extends VisualParent {
		public final VisualGroup target;
		public int index;

		public Parent(final VisualGroup target, final int index) {
			this.target = target;
			this.index = index;
		}

		@Override
		public VisualParent parent() {
			return target.parent;
		}

		@Override
		public Brick createNextBrick(final Context context) {
			if (index + 1 < target.children.size())
				return target.children.get(index + 1).createFirstBrick(context);
			if (target.parent == null)
				return null;
			return target.parent.createNextBrick(context);
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			if (index > 0)
				return target.children.get(index - 1).createLastBrick(context);
			if (target.parent == null)
				return null;
			return target.parent.createPreviousBrick(context);
		}

		@Override
		public Visual visual() {
			return target;
		}

		@Override
		public VisualAtomType atomVisual() {
			if (target.parent == null)
				return null;
			return target.parent.atomVisual();
		}

		@Override
		public Alignment getAlignment(final String alignment) {
			return target.getAlignment(alignment);
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			if (index == 0)
				if (target.parent == null)
					return null;
				else
					return target.parent.getPreviousBrick(context);
			else
				return target.children.get(index - 1).getLastBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			if (index + 1 >= target.children.size())
				if (target.parent == null)
					return null;
				else
					return target.parent.getNextBrick(context);
			else
				return target.children.get(index + 1).getFirstBrick(context);
		}

		@Override
		public Hoverable hover(
				final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector point
		) {
			return target.hover(context, point);
		}

		public int getIndex() {
			return index;
		}
	}
}
