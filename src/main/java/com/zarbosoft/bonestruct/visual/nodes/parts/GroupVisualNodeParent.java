package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;

public class GroupVisualNodeParent extends VisualNodeParent {
	public final GroupVisualNode target;
	int index;

	public GroupVisualNodeParent(final GroupVisualNode target, final int index) {
		this.target = target;
		this.index = index;
	}

	@Override
	public void adjust(final Context context, final VisualNode.Adjustment adjustment) {
		target.getIdle(context);
		GroupVisualNode.ChildChange change = target.idle.childChanges.get(index);
		if (change == null) {
			change = new GroupVisualNode.ChildChange();
			target.idle.childChanges.put(index, change);
		}
		if (adjustment.converseEdge != null)
			change.converseEdge = adjustment.converseEdge;
		if (adjustment.transverseEdge != null)
			change.transverseEdge = adjustment.transverseEdge;
		if (adjustment.converseEnd != null)
			change.converseEnd = adjustment.converseEnd;
		if (adjustment.transverseEnd != null)
			change.transverseEnd = adjustment.transverseEnd;
	}

	@Override
	public VisualNodeParent parent() {
		if (target.parent() == null)
			return null;
		return target.parent();
	}

	@Override
	public VisualNodePart target() {
		return target;
	}

	@Override
	public void align(final Context context) {
		target.getIdle(context);
		GroupVisualNode.ChildChange change = target.idle.childChanges.get(index);
		if (change == null) {
			change = new GroupVisualNode.ChildChange();
			target.idle.childChanges.put(index, change);
		}
		change.alignment = true;
	}

	@Override
	public Context.Hoverable hoverUp(final Context context) {
		return parent().hoverUp(context);
	}

	@Override
	public void selectUp(final Context context) {
		parent().selectUp(context);
	}
}
