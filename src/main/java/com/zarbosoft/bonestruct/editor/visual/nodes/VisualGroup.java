package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.function.IntFunction;

import static com.zarbosoft.rendaw.common.Common.last;

public class VisualGroup extends VisualNodePart {
	public VisualGroup(final Set<Tag> tags) {
		super(tags);
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
	public boolean select(final Context context) {
		for (final VisualNodePart child : children) {
			if (child.select(context))
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
	public VisualNodeParent parent = null;

	// State
	IdleTask idle;
	protected List<VisualNodePart> children = new ArrayList<>();
	boolean compact = false;

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	public void add(final Context context, final VisualNodePart node, int preindex) {
		if (preindex < 0)
			preindex = this.children.size() + preindex + 1;
		if (preindex >= this.children.size() + 1)
			throw new AssertionError("Inserting visual node after group end.");
		final int index = preindex;
		this.children.stream().skip(index).forEach(n -> ((Parent) n.parent()).index += 1);
		this.children.add(index, node);
		node.setParent(createParent(index));
		final Brick previousBrick = index == 0 ?
				(parent == null ? null : parent.getPreviousBrick(context)) :
				children.get(index - 1).getLastBrick(context);
		final Brick nextBrick = index + 1 >= this.children.size() ?
				(parent == null ? null : parent.getNextBrick(context)) :
				children.get(index + 1).getFirstBrick(context);
		if (previousBrick != null && nextBrick != null)
			context.fillFromEndBrick(previousBrick);
	}

	protected VisualNodeParent createParent(final int index) {
		return new Parent(this, index);
	}

	public void add(final Context context, final VisualNodePart node) {
		add(context, node, -1);
	}

	public void remove(final Context context, int preindex) {
		if (preindex < 0)
			preindex = this.children.size() + preindex;
		if (preindex >= this.children.size())
			throw new AssertionError("Removing visual node after group end.");
		final Integer index = preindex;
		final VisualNodePart node = children.get(index);
		node.destroy(context);
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
	public Iterator<VisualNode> children() {
		return Iterators.concat(children
				.stream()
				.map(c -> c.children())
				.toArray((IntFunction<Iterator<VisualNode>[]>) Iterator[]::new));
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
	public void rootAlignments(final Context context, final Map<String, Alignment> alignments) {
		PMap<String, Alignment> derived = HashTreePMap.from(alignments);
		for (final Map.Entry<String, Alignment> e : this.alignments.entrySet()) {
			final Alignment alignment = e.getValue();
			alignment.root(context, alignments);
			derived = derived.plus(e.getKey(), alignment);
		}
		for (final VisualNodePart child : children)
			child.rootAlignments(context, derived);
	}

	@Override
	public void destroy(final Context context) {
		for (final VisualNodePart child : children)
			child.destroy(context);
	}

	public static class Parent extends VisualNodeParent {
		public final VisualGroup target;
		public int index;

		public Parent(final VisualGroup target, final int index) {
			this.target = target;
			this.index = index;
		}

		@Override
		public void selectUp(final Context context) {
			if (target.parent == null)
				return;
			target.parent.selectUp(context);
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
		public VisualNode getTarget() {
			return target;
		}

		@Override
		public NodeType.NodeTypeVisual getNode() {
			if (target.parent == null)
				return null;
			return target.parent.getNode();
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
		public Context.Hoverable hover(
				final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector point
		) {
			return target.hover(context, point);
		}

		public int getIndex() {
			return index;
		}
	}
}
