package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.scene.Group;

import java.util.*;
import java.util.stream.Collectors;

public class Course {

	public int index;
	public Group visual = new Group();
	public Group brickVisual = new Group();
	public Wall parent;
	private IdlePlaceTask idlePlace;
	private IdleCompactTask idleCompact;
	private IdleExpandTask idleExpand;
	int transverseStart = 0;
	int ascent = 0;
	int descent = 0;
	public List<Brick> children = new ArrayList<>();
	//Fixture fixtures[] = new Fixture[2];
	private final Map<Brick, Set<Attachment>> attachments = new HashMap<>();
	int lastExpandCheckConverse = 0;

	public Course(final Context context) {
		visual.getChildren().add(0, brickVisual);
	}

	public int transverseEdge(final Context context) {
		final int out = transverseStart + ascent + descent;
		/*
		for (final Fixture f : fixtures) {
			out += f.transverseSpan(context);
		}
		*/
		return out;
	}

	public void setTransverse(final Context context, final int transverse) {
		transverseStart = transverse;
		context.translate(visual, new Vector(0, transverseStart));
		parent.adjust(context, index);
		for (final Map.Entry<Brick, Set<Attachment>> pair : attachments.entrySet()) {
			for (final Attachment attachment : pair.getValue())
				attachment.setTransverse(context, transverseStart);
		}
	}

	public void changed(final Context context, final int at) {
		final Brick brick = children.get(at);
		final Brick.Properties properties = brick.properties();
		if (at > 0 && properties.broken) {
			breakCourse(context, at);
			return;
		} else if (at == 0 && !properties.broken && this.index > 0) {
			joinPreviousCourse(context);
			return;
		}
		getIdlePlace(context);
		idlePlace.at(at);
		idlePlace.changed.add(brick);
	}

	public void attachmentsChanged(final Context context, final int at) {
		final Brick brick = children.get(at);
		attachments.put(brick, brick.getAttachments(context));
	}

	private void joinPreviousCourse(final Context context) {
		visual.getChildren().clear();
		final Course previous = parent.children.get(this.index - 1);
		previous.add(context, previous.children.size(), children);
		destroyInner(context);
	}

	public Course breakCourse(final Context context, final int index) {
		if (index == 0)
			throw new AssertionError("Breaking course at 0.");
		final Course next = new Course(context);
		parent.add(context, this.index + 1, ImmutableList.of(next));
		if (index < children.size()) {
			final List<Brick> transplant = ImmutableList.copyOf(children.subList(index, children.size()));
			getIdlePlace(context);
			for (final Brick brick : transplant) {
				idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.properties().ascent);
				idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.properties().descent);
				idlePlace.changed.remove(brick);
				attachments.remove(brick);
			}
			children.subList(index, children.size()).clear();
			visual.getChildren().remove(index, visual.getChildren().size());
			next.add(context, 0, transplant);
		}
		return next;
	}

	/*
	public void fixtureChanged(final Context context, final int index) {
		if (index != 0 && index != 1)
			throw new AssertionError("Invalid fixture index");
		getIdlePlace(context);
		idlePlace.fixtures[index] = true;
	}

	public void setFixture(final Context context, final int index, final Fixture fixture) {
		if (index != 0 && index != 1)
			throw new AssertionError("Invalid fixture index");
		if (fixtures[index] != null) {
			visual.getChildren().remove(index == 0 ? 0 : fixtures[0] == null ? 1 : 2, 1);
			fixtures[index].parent = null;
		}
		fixtures[index] = fixture;
		if (fixture != null) {
			fixtures[index].parent = this;
			fixtures[index].index = index;
			visual.getChildren().add(index == 0 ? 0 : fixtures[0] == null ? 1 : 2, fixture.visual(context));
		}
		getIdlePlace(context);
		idlePlace.fixtures[index] = true;
	}
	*/

	public void add(final Context context, final int at, final List<Brick> bricks) {
		if (bricks.size() == 0)
			throw new AssertionError("Adding no bricks");
		children.addAll(at, bricks);
		for (final Brick brick : bricks)
			brick.parent = this;
		renumber(at);
		visual.getChildren().addAll(at, bricks.stream().map(c -> c.getRawVisual()).collect(Collectors.toList()));
		for (int i = 0; i < bricks.size(); ++i) {
			final Brick brick = bricks.get(i);
			attachments.put(brick, ImmutableSet.copyOf(brick.getAttachments(context)));
			for (final Attachment attachment : ImmutableSet.copyOf(brick.getAttachments(context)))
				attachment.setTransverse(context, transverseStart);
		}
		getIdlePlace(context);
		idlePlace.at(at);
		idlePlace.changed.addAll(bricks);
	}

	public void removeFromSystem(final Context context, final int at) {
		final Brick brick = children.get(at);
		children.remove(at);
		if (children.isEmpty()) {
			destroyInner(context);
		} else {
			if (at == 0) {
				joinPreviousCourse(context);
			} else {
				brick.parent = null;
				attachments.remove(brick);
				visual.getChildren().remove(at);
				getIdlePlace(context);
				idlePlace.at(at);
				idlePlace.removeMaxAscent = Math.max(idlePlace.removeMaxAscent, brick.properties().ascent);
				idlePlace.removeMaxDescent = Math.max(idlePlace.removeMaxDescent, brick.properties().descent);
				idlePlace.changed.remove(brick);
			}
		}
	}

	private void destroyInner(final Context context) {
		if (idlePlace != null)
			idlePlace.destroy();
		if (idleCompact != null)
			idleCompact.destroy();
		if (idleExpand != null)
			idleExpand.destroy();
		parent.remove(context, index);
	}

	public void destroy(final Context context) {
		while (!children.isEmpty())
			Helper.last(children).destroy(context);
	}

	private void renumber(final int at) {
		for (int index = at; index < children.size(); ++index) {
			children.get(index).index = index;
		}
	}

	private void getIdlePlace(final Context context) {
		if (idlePlace == null) {
			idlePlace = new IdlePlaceTask(context);
			context.addIdle(idlePlace);
		}
	}

	private void getIdleCompact(final Context context) {
		if (idleCompact == null) {
			idleCompact = new IdleCompactTask(context);
			context.addIdle(idleCompact);
		}
	}

	private void getIdleExpand(final Context context) {
		if (idleExpand == null) {
			idleExpand = new IdleExpandTask(context);
			context.addIdle(idleExpand);
		}
	}

	class IdlePlaceTask extends com.zarbosoft.bonestruct.editor.visual.IdleTask {

		private final Context context;
		int first = Integer.MAX_VALUE;
		Set<Brick> changed = new HashSet<>();
		int removeMaxAscent = 0;
		int removeMaxDescent = 0;
		//boolean fixtures[] = {false, false};

		public IdlePlaceTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 170;
		}

		@Override
		public void runImplementation() {
			// Update attachments
			changed.stream().forEach(b -> attachments.put(b, ImmutableSet.copyOf(b.getAttachments(context))));

			// Update transverse space
			boolean newAscent = false, newDescent = false;
			for (final Brick brick : changed) {
				final Brick.Properties properties = brick.properties();
				if (properties.ascent > ascent) {
					ascent = properties.ascent;
					newAscent = true;
				}
				if (properties.descent > descent) {
					descent = properties.descent;
					newDescent = true;
				}
			}
			if (!(newAscent && newDescent) && removeMaxAscent == ascent && removeMaxDescent == descent) {
				ascent = 0;
				descent = 0;
				{
					for (final Brick brick : children) {
						final Brick.Properties properties = brick.properties();
						ascent = Math.max(ascent, properties.ascent);
						descent = Math.max(descent, properties.descent);
					}
				}
				newAscent = true;
				newDescent = true;
			}
			if (newAscent || newDescent) {
				children.stream().forEach(b -> {
					b.allocateTransverse(context, ascent, descent);
					b.getAttachments(context).forEach(a -> a.setTransverseSpan(context, ascent, descent));
				});
			} else
				changed.stream().forEach(b -> {
					b.allocateTransverse(context, ascent, descent);
					b.getAttachments(context).forEach(a -> a.setTransverseSpan(context, ascent, descent));
				});

			/*
			if (fixtures[0]) {
				final boolean noFirst = Course.this.fixtures[0] == null;
				final int offset = noFirst ? 0 : Course.this.fixtures[0].transverseSpan(context);
				context.translate(brickVisual, new Vector(0, offset));
				if (Course.this.fixtures[1] != null)
					context.translate(noFirst ? visual.getChildren().get(1) : visual.getChildren().get(2),
							new Vector(0, offset + ascent + descent)
					);
			}
			*/

			// Do getConverse placement
			final Set<Alignment> seenAlignments = new HashSet<>();
			final int at = first;
			int converse = at == 0 ?
					0 :
					(at >= children.size() ? Helper.last(children) : children.get(at - 1)).converseEdge(context);
			for (int index = at; index < children.size(); ++index) {
				final Brick brick = children.get(index);
				final Brick.Properties properties = brick.properties();
				final int minConverse = converse;
				if (properties.alignment != null && !seenAlignments.contains(properties.alignment)) {
					seenAlignments.add(properties.alignment);
					converse = Math.max(converse, properties.alignment.converse);
					properties.alignment.set(context, converse);
				}
				brick.setConverse(context, minConverse, converse);
				for (final Attachment attachment : brick.getAttachments(context))
					attachment.setConverse(context, converse);
				converse = brick.converseEdge(context);
				if (converse > context.edge)
					getIdleCompact(context);
			}
			if (converse < lastExpandCheckConverse)
				getIdleExpand(context);

			// Propagate changes up
			if (newAscent || newDescent/* || fixtures[0] || fixtures[1]*/)
				parent.adjust(context, at);

			idlePlace = null;
		}

		@Override
		protected void destroyed() {
			idlePlace = null;
		}

		public void at(final int at) {
			if (at < first)
				first = at;
		}
	}

	public boolean compact(final Context context) {
		final PriorityQueue<VisualNode> priorities = new PriorityQueue<>(11, new Comparator<VisualNode>() {
			@Override
			public int compare(final VisualNode o1, final VisualNode o2) {
				return Integer.compare(o1.spacePriority(), o2.spacePriority());
			}
		});
		int converse = 0;
		for (int index = 0; index < children.size(); ++index) {
			final Brick brick = children.get(index);
			final VisualNode node = brick.getNode();
			if (node.canCompact())
				priorities.add(node);
			converse = brick.converseEdge(context);
			if (!priorities.isEmpty() && converse > context.edge)
				break;
		}
		if (converse < context.edge) {
			lastExpandCheckConverse = converse;
			return false;
		}
		if (priorities.isEmpty()) {
			return false;
		}
		priorities.poll().compact(context);
		return true;
	}

	class IdleCompactTask extends com.zarbosoft.bonestruct.editor.visual.IdleTask {
		private final Context context;

		IdleCompactTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return 150;
		}

		@Override
		public void runImplementation() {
			if (compact(context)) {
				context.addIdle(this);
			} else {
				idleCompact = null;
			}
		}

		@Override
		protected void destroyed() {
			idleCompact = null;
		}
	}

	public boolean expand(final Context context) {
		final PriorityQueue<VisualNode> priorities = new PriorityQueue<>(11, new Comparator<VisualNode>() {
			@Override
			public int compare(final VisualNode o1, final VisualNode o2) {
				return Integer.compare(o2.spacePriority(), o1.spacePriority());
			}
		});
		for (int index = 0; index < children.size(); ++index) {
			final Brick brick = children.get(index);
			final VisualNode node = brick.getNode();
			if (node.canExpand())
				priorities.add(node);
		}
		if (priorities.isEmpty()) {
			// Row is empty (new doc only?)
			return false;
		}
		final VisualNode top = priorities.poll();
		final Iterable<Pair<Brick, Brick.Properties>> brickProperties = top.getPropertiesForTagsChange(context,
				new VisualNode.TagsChange(ImmutableSet.of(new VisualNode.StateTag("expanded")),
						ImmutableSet.of(new VisualNode.StateTag("compact"))
				)
		);
		int converse = 0;
		Course lastCourse = null;
		Course effectiveCourse = null;
		for (final Pair<Brick, Brick.Properties> pair : brickProperties) {
			final Brick brick = pair.first;
			final Brick.Properties properties = pair.second;
			if (lastCourse == null) {
				effectiveCourse = lastCourse = brick.parent;
				converse = 0;
			} else if (brick.parent != lastCourse) {
				lastCourse = brick.parent;
				final boolean join = brick.index == 0 && properties.broken;
				if (!join || lastCourse.parent.children.get(lastCourse.index - 1) != effectiveCourse) {
					converse = 0;
					effectiveCourse = brick.parent;
				}
			}

			if (properties.alignment != null) {
				converse += Math.max(converse, properties.alignment.converse);
			}
			converse += properties.converseSpan;
			if (converse > context.edge) {
				// Not enough space, stop
				return false;
			}
		}
		if (lastCourse == null) {
			// Nothing left to expand
			return false;
		}
		top.expand(context);
		return true;
	}

	class IdleExpandTask extends com.zarbosoft.bonestruct.editor.visual.IdleTask {
		private final Context context;

		IdleExpandTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return -95;
		}

		@Override
		public void runImplementation() {
			if (expand(context)) {
				context.addIdle(this);
			} else {
				idleExpand = null;
			}
		}

		@Override
		protected void destroyed() {
			idleExpand = null;
		}
	}
}
