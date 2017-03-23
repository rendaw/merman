package com.zarbosoft.bonestruct.editor.visual.wall;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.IdleTask;
import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;

public class Wall {
	public Group visual = new Group();
	public List<Course> children = new ArrayList<>();
	private IdleAdjustTask idleAdjust;
	private IdleCompactTask idleCompact;
	private IdleExpandTask idleExpand;
	private Course cornerstoneCourse;

	public void clear(final Context context) {
		while (!children.isEmpty())
			last(children).destroy(context);
		if (idleCompact != null)
			idleCompact.destroy();
		if (idleExpand != null)
			idleExpand.destroy();
		if (idleAdjust != null)
			idleAdjust.destroy();
	}

	private void renumber(final int at) {
		for (int index = at; index < children.size(); ++index) {
			children.get(index).index = index;
		}
	}

	private void getIdle(final Context context) {
		if (idleAdjust == null) {
			idleAdjust = new IdleAdjustTask(context);
			context.addIdle(idleAdjust);
		}
	}

	void add(final Context context, final int at, final List<Course> courses) {
		final boolean adjustForward = cornerstoneCourse == null ? true : at > cornerstoneCourse.index;
		children.addAll(at, courses);
		courses.stream().forEach(l -> l.parent = this);
		renumber(at);
		visual.getChildren().addAll(at, courses.stream().map(l -> l.visual).collect(Collectors.toList()));
		getIdle(context);
		if (children.size() > 1) {
			if (idleAdjust.backward >= at)
				idleAdjust.backward += 1;
			if (idleAdjust.forward >= at && idleAdjust.forward < Integer.MAX_VALUE)
				idleAdjust.forward += 1;
			idleAdjust.at(at);
		}
	}

	void remove(final Context context, final int at) {
		final boolean adjustForward = at > cornerstoneCourse.index;
		children.remove(at);
		visual.getChildren().remove(at);
		if (at < children.size()) {
			renumber(at);
			getIdle(context);
			if (at < idleAdjust.backward)
				idleAdjust.backward -= 1;
			if (at < idleAdjust.forward && idleAdjust.forward < Integer.MAX_VALUE)
				idleAdjust.forward -= 1;
			idleAdjust.at(at);
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

	public void setCornerstone(final Context context, final Brick cornerstone) {
		if (cornerstone.parent == null) {
			clear(context);
			final Course course = new Course(context);
			add(context, 0, ImmutableList.of(course));
			course.add(context, 0, ImmutableList.of(cornerstone));
		}
		this.cornerstoneCourse = cornerstone.parent;
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

	class IdleAdjustTask extends IdleTask {
		final private Context context;
		int forward = Integer.MAX_VALUE;
		int backward = Integer.MIN_VALUE;

		IdleAdjustTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 160;
		}

		@Override
		public void runImplementation() {
			boolean modified = false;
			if (cornerstoneCourse.index <= backward || cornerstoneCourse.index >= forward) {
				backward = cornerstoneCourse.index - 1;
				forward = cornerstoneCourse.index + 1;
			}
			if (backward >= 0) {
				// Always < children size because of cornerstone
				final Course child = children.get(backward);
				final Course preceding = children.get(backward + 1);
				child.setTransverse(context,
						preceding.transverseStart - preceding.beddingBefore - child.transverseSpan()
				);
				backward -= 1;
				modified = true;
			}
			if (forward < children.size()) {
				// Always > 0 because of cornerstone
				children.get(forward).setTransverse(context, children.get(forward - 1).transverseEdge(context));
				forward += 1;
				modified = true;
			}
			if (!modified) {
				idleAdjust = null;
			} else {
				context.addIdle(this);
			}
		}

		@Override
		protected void destroyed() {
			idleAdjust = null;
		}

		public void at(final int at) {
			if (at <= cornerstoneCourse.index && at > backward)
				backward = Math.min(cornerstoneCourse.index - 1, at);
			if (at >= cornerstoneCourse.index && at < forward)
				forward = Math.max(cornerstoneCourse.index + 1, at);
		}
	}

	void adjust(final Context context, final int at) {
		getIdle(context);
		idleAdjust.at(at);
	}
}
