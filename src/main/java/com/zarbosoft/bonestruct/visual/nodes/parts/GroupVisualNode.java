package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.visual.Brick;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.IdleTask;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.VisualNodeParent;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public class GroupVisualNode extends VisualNodePart {
	public GroupVisualNode(final Set<Tag> tags) {
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
		return Helper.last(children).getFirstBrick(context);
	}

	/*
	public Context.Hoverable hover(final Context context, final Vector point) {
		for (final VisualNodePart child : children) {
			final Context.Hoverable out = child.hover(context, point);
			if (out != null)
				return out;
		}
		return null;
	}
	*/

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

	public Map<String, Alignment> alignments = new HashMap<>();
	VisualNodeParent parent = null;

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
			throw new RuntimeException("Inserting visual node after group end.");
		final int index = preindex;
		this.children.stream().skip(index).forEach(n -> ((GroupVisualNodeParent) n.parent()).index += 1);
		node.setParent(new GroupVisualNodeParent(this, index));
		this.children.add(index, node);
		final Brick previousBrick = index == 0 ?
				(parent == null ? null : parent.getPreviousBrick(context)) :
				children.get(index - 1).getLastBrick(context);
		final Brick nextBrick = index == this.children.size() - 1 ?
				(parent == null ? null : parent.getNextBrick(context)) :
				children.get(index + 1).getFirstBrick(context);
		if (previousBrick != null && nextBrick != null)
			context.fillFromEndBrick(previousBrick);
	}

	public void add(final Context context, final VisualNodePart node) {
		add(context, node, -1);
	}

	public void remove(final Context context, int preindex) {
		if (preindex < 0)
			preindex = this.children.size() + preindex;
		if (preindex >= this.children.size() - 1)
			throw new RuntimeException("Removing visual node after group end.");
		final Integer index = preindex;
		final VisualNodePart node = children.get(index);
		node.destroyBricks(context);
		this.children.remove(index);
		this.children.stream().skip(index).forEach(n -> ((GroupVisualNodeParent) n.parent()).index -= 1);
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
	public String debugTreeType() {
		return String.format("group@%s", Integer.toHexString(hashCode()));
	}

	public String debugTree(final int indent) {
		final String indentString = String.join("", Collections.nCopies(indent, "  "));
		return String.format(
				"%s%s%s",
				indentString,
				debugTreeType(),
				children
						.stream()
						.map(c -> String.format("\n%s", c.debugTree(indent + 1)))
						.collect(Collectors.joining(""))
		);
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
			final Alignment parent = alignments.get(e.getKey());
			final Alignment alignment = e.getValue();
			if (parent != null)
				parent.place(context, parent);
			derived = derived.plus(e.getKey(), alignment);
		}
		for (final VisualNodePart child : children)
			child.rootAlignments(context, derived);
	}

	@Override
	public void destroyBricks(final Context context) {
		for (final VisualNodePart child : children)
			child.destroyBricks(context);
	}
}
