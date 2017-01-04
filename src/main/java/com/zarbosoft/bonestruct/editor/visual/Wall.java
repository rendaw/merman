package com.zarbosoft.bonestruct.editor.visual;

import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Wall {
	public Group visual = new Group();
	List<Course> children = new ArrayList<>();
	private AdjustIdleTask adjustIdle;
	private IdleCompactTask idleCompact;
	private IdleExpandTask idleExpand;

	public void clear(final Context context) {
		for (final Course course : children) {
			course.clear(context);
		}
		children.clear();
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

	public void remove(final Context context, final int at, final int count) {
		for (int i = 0; i < count; ++i)
			children.remove(at + i);
		visual.getChildren().remove(at, at + count);
		if (at < children.size()) {
			renumber(at);
		}
		getIdle(context, at);
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
		public void run() {
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
		public void run() {
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
		public void run() {
			if (at + 1 >= children.size()) {
				adjustIdle = null;
				return;
			}
			children.get(at + 1).setTransverse(context, children.get(at).transverseEdge(context));
			at += 1;
			context.addIdle(this);
		}
	}

	void adjust(final Context context, final int at) {
		getIdle(context, at);
	}
}
