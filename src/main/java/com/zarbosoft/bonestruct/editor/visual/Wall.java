package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.pidgoon.internal.Helper;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Wall {
	public Group visual = new Group();
	public List<Course> children = new ArrayList<>();
	private AdjustIdleTask adjustIdle;
	private IdleCompactTask idleCompact;
	private IdleExpandTask idleExpand;

	public void clear(final Context context) {
		while (!children.isEmpty())
			Helper.last(children).destroy(context);
		if (idleCompact != null)
			idleCompact.destroy();
		if (idleExpand != null)
			idleExpand.destroy();
		if (adjustIdle != null)
			adjustIdle.destroy();
	}

	private void renumber(final int at) {
		for (int index = at; index < children.size(); ++index) {
			children.get(index).index = index;
		}
	}

	private void getIdle(final Context context, final int at) {
		if (adjustIdle == null) {
			adjustIdle = new AdjustIdleTask(context);
			context.addIdle(adjustIdle);
		}
		if (at < adjustIdle.at)
			adjustIdle.at = at;
	}

	public void add(final Context context, final int at, final List<Course> courses) {
		children.addAll(at, courses);
		courses.stream().forEach(l -> l.parent = this);
		renumber(at);
		visual.getChildren().addAll(at, courses.stream().map(l -> l.visual).collect(Collectors.toList()));
		getIdle(context, at);
	}

	public void remove(final Context context, final int at) {
		children.remove(at);
		visual.getChildren().remove(at);
		if (at < children.size()) {
			renumber(at);
			getIdle(context, at);
		}
	}

	public void idleCompact(final Context context) {
		if (idleCompact == null) {
			idleCompact = new IdleCompactTask(context);
			context.addIdle(idleCompact);
		}
		idleCompact.at = 0;
	}

	public void idleExpand(final Context context) {
		if (idleExpand == null) {
			idleExpand = new IdleExpandTask(context);
			context.addIdle(idleExpand);
		}
		idleExpand.at = 0;
	}

	class IdleCompactTask extends com.zarbosoft.bonestruct.editor.visual.IdleTask {
		private final Context context;
		int at = 0;

		IdleCompactTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 110;
		}

		@Override
		public void runImplementation() {
			if (at >= children.size()) {
				idleCompact = null;
				return;
			}
			if (children.get(at).compact(context)) {
			} else {
				at += 1;
			}
			context.addIdle(this);
		}

		@Override
		protected void destroyed() {
			idleCompact = null;
		}
	}

	class IdleExpandTask extends com.zarbosoft.bonestruct.editor.visual.IdleTask {
		private final Context context;
		int at = 0;

		IdleExpandTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return -100;
		}

		@Override
		public void runImplementation() {
			if (at >= children.size()) {
				idleExpand = null;
				return;
			}
			if (children.get(at).expand(context)) {
			} else {
				at += 1;
			}
			context.addIdle(this);
		}

		@Override
		protected void destroyed() {
			idleExpand = null;
		}
	}

	class AdjustIdleTask extends IdleTask {
		final private Context context;
		int at = Integer.MAX_VALUE;

		AdjustIdleTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 160;
		}

		@Override
		public void runImplementation() {
			if (at >= children.size()) {
				adjustIdle = null;
				return;
			}
			if (at == 0)
				children.get(at).setTransverse(context, 0);
			else
				children.get(at).setTransverse(context, children.get(at - 1).transverseEdge(context));
			at += 1;
			context.addIdle(this);
		}

		@Override
		protected void destroyed() {
			adjustIdle = null;
		}
	}

	void adjust(final Context context, final int at) {
		getIdle(context, at);
	}
}
