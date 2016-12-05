package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Brick;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.VisualNodeParent;

public class GroupVisualNodeParent extends VisualNodeParent {
	public final GroupVisualNode target;
	int index;

	public GroupVisualNodeParent(final GroupVisualNode target, final int index) {
		this.target = target;
		this.index = index;
	}

	/*
	@Override
	public Context.Hoverable hoverUp(final Context context) {
		return parent().hoverUp(context);
	}
	*/

	@Override
	public void selectUp(final Context context) {
		if (target.parent == null)
			return;
		target.parent.selectUp(context);
	}

	@Override
	public Brick createNextBrick(final Context context) {
		for (int i = index + 1; i < target.children.size(); ++i) {
			final Brick brick = target.children.get(i).createFirstBrick(context);
			if (brick != null)
				return brick;
		}
		if (target.parent == null)
			return null;
		return target.parent.createNextBrick(context);
	}

	@Override
	public VisualNode getNode() {
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
		if (index == target.children.size())
			if (target.parent == null)
				return null;
			else
				return target.parent.getNextBrick(context);
		else
			return target.children.get(index + 1).getFirstBrick(context);
	}
}
