package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.rendaw.common.Pair;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Map;
import java.util.Set;

public class EmbeddedNestedVisualNodePart extends VisualNodePart {
	final DataNode.Value data;
	protected VisualNode body;
	VisualNodeParent parent;

	public EmbeddedNestedVisualNodePart(final Context context, final DataNode.Value data, final Set<Tag> tags) {
		super(tags);
		this.data = data;
		data.addListener(new DataNode.Listener() {

			@Override
			public void set(final Context context, final Node node) {
				EmbeddedNestedVisualNodePart.this.set(context, node);
			}

		});
		set(context, data.get());
	}

	protected VisualNodeParent createParent() {
		return new NestedParent();
	}

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	@Override
	public boolean select(final Context context) {
		return false;
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		return body.createFirstBrick(context);
	}

	@Override
	public Brick createLastBrick(final Context context) {
		return body.createLastBrick(context);
	}

	void set(final Context context, final Node data) {
		if (body != null)
			body.destroyBricks(context);
		this.body = data.createVisual(context);
		body.setParent(createParent());
		if (parent != null) {
			final Brick previousBrick = parent.getPreviousBrick(context);
			final Brick nextBrick = parent.getNextBrick(context);
			if (previousBrick != null && nextBrick != null)
				context.fillFromEndBrick(previousBrick);
		}
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
	public void rootAlignments(final Context context, final Map<String, Alignment> alignments) {
		body.rootAlignments(context, alignments);
	}

	@Override
	public void destroyBricks(final Context context) {
		body.destroyBricks(context);
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return body.getPropertiesForTagsChange(context, change);
	}

	class NestedParent extends VisualNodeParent {
		@Override
		public void selectUp(final Context context) {
			select(context);
		}

		@Override
		public Brick createNextBrick(final Context context) {
			if (parent == null)
				return null;
			return parent.createNextBrick(context);
		}

		@Override
		public VisualNode getTarget() {
			return EmbeddedNestedVisualNodePart.this;
		}

		@Override
		public NodeType.NodeTypeVisual getNode() {
			throw new NotImplementedException();
		}

		@Override
		public Alignment getAlignment(final String alignment) {
			return parent.getAlignment(alignment);
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			if (parent == null)
				return null;
			return parent.getPreviousBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			if (parent == null)
				return null;
			return parent.getNextBrick(context);
		}

		@Override
		public Context.Hoverable hover(final Context context, final Vector point) {
			if (parent == null)
				return null;
			return parent.hover(context, point);
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			if (parent == null)
				return null;
			return parent.createPreviousBrick(context);
		}
	}
}
