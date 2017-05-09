package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.enumerate;

public class VisualNodeType extends Visual {
	private final NodeType nodeType;
	private final VisualGroup body;
	private final Node node;
	private boolean compact;
	private VisualParent parent;
	public int depth = 0;

	public VisualNodeType(final NodeType nodeType, final Context context, final Node node) {
		super(HashTreePSet.<Tag>empty().plus(new TypeTag(nodeType.id)).plus(new PartTag("node")));
		this.nodeType = nodeType;
		this.node = node;
		final PSet<Tag> tags = HashTreePSet.singleton(new TypeTag(nodeType.id));
		compact = false;
		body = new VisualGroup(HashTreePSet.empty());
		for (final Map.Entry<String, AlignmentDefinition> entry : nodeType.alignments().entrySet()) {
			body.alignments.put(entry.getKey(), entry.getValue().create());
		}
		enumerate(Common.stream(nodeType.front())).forEach(pair -> {
			final VisualPart visual = pair.second.createVisual(context, node, tags);
			body.add(context, visual);
		});
		body.setParent(new VisualParent() {

			@Override
			public void selectUp(final Context context) {
				parent.selectUp(context);
			}

			@Override
			public Brick createNextBrick(final Context context) {
				if (context.window == VisualNodeType.this)
					return null;
				return parent.createNextBrick(context);
			}

			@Override
			public Brick createPreviousBrick(final Context context) {
				if (context.window == VisualNodeType.this)
					return null;
				return parent.createPreviousBrick(context);
			}

			@Override
			public Visual getTarget() {
				return parent.getTarget();
			}

			@Override
			public VisualNodeType getNodeVisual() {
				return VisualNodeType.this;
			}

			@Override
			public Alignment getAlignment(final String alignment) {
				return parent.getAlignment(alignment);
			}

			@Override
			public Brick getPreviousBrick(final Context context) {
				if (context.window == VisualNodeType.this)
					return null;
				if (parent == null)
					return null;
				return parent.getPreviousBrick(context);
			}

			@Override
			public Brick getNextBrick(final Context context) {
				if (context.window == VisualNodeType.this)
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
		});
		node.visual = this;
	}

	@Override
	public void setParent(final VisualParent parent) {
		this.parent = parent;
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
	public void select(final Context context) {
		throw new DeadCode();
	}

	@Override
	public void selectUp(final Context context) {
		if (parent == null)
			return;
		parent.selectUp(context);
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
		return -nodeType.precedence();
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

	@Override
	public void anchor(
			final Context context, final Map<String, Alignment> alignments, final int depth
	) {
		this.depth = depth;
		body.anchor(context, alignments, depth + nodeType.depthScore);
	}

	@Override
	public void destroy(final Context context) {
		node.visual = null;
		body.destroy(context);
	}

	@Override
	public boolean isAt(final Value value) {
		return false;
	}

	@Override
	public void tagsChanged(final Context context) {

	}

	public NodeType getType() {
		return nodeType;
	}
}
