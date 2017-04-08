package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.bonestruct.syntax.NodeType;

public class GroupVisualNodeParent extends VisualNodeParent {
	public final GroupVisualNode target;
	public int index;

	public GroupVisualNodeParent(final GroupVisualNode target, final int index) {
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
	public Context.Hoverable hover(final Context context, final Vector point) {
		return target.hover(context, point);
	}

	public int getIndex() {
		return index;
	}
}
