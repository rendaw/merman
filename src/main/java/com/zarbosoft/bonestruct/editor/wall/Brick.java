package com.zarbosoft.bonestruct.editor.wall;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.VisualLeaf;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TagsChange;
import com.zarbosoft.bonestruct.syntax.style.Style;

import java.util.HashSet;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.last;

public abstract class Brick {
	public Course parent;
	public int index;
	Set<Attachment> attachments = new HashSet<>();
	public Style.Baked style;
	public final BrickInterface inter;

	protected Brick(final BrickInterface inter) {
		this.inter = inter;
	}

	public abstract int getConverse(Context context);

	public abstract int converseEdge(final Context context);

	public abstract DisplayNode getDisplayNode();

	public abstract void setConverse(Context context, int minConverse, int converse);

	public abstract void tagsChanged(Context context);

	public abstract Properties properties(final Context context, final Style.Baked style);

	/**
	 * @param context
	 * @return A new brick or null (no elements before or brick already exists)
	 */
	public Brick createPrevious(final Context context) {
		return inter.createPrevious(context);
	}

	/**
	 * @param context
	 * @return A new brick or null (no elements afterward or brick already exists)
	 */
	public Brick createNext(final Context context) {
		return inter.createNext(context);
	}

	protected Style.Baked getStyle() {
		return style;
	}

	public Set<Tag> getTags(final Context context) {
		return inter.getTags(context);
	}

	public Properties properties(final Context context) {
		return properties(context, getStyle());
	}

	public Properties getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return properties(context, context.getStyle(change.apply(inter.getTags(context))));
	}

	public Hoverable hover(final Context context, final Vector point) {
		return inter.getVisual().hover(context, point);
	}

	public VisualLeaf getVisual() {
		return inter.getVisual();
	}

	public static class Properties {
		public final boolean split;
		public final int ascent;
		public final int descent;
		public final Alignment alignment;
		public final int converseSpan;

		public Properties(
				final boolean split,
				final int ascent,
				final int descent,
				final Alignment alignment,
				final int converseSpan
		) {
			this.split = split;
			this.ascent = ascent;
			this.descent = descent;
			this.alignment = alignment;
			this.converseSpan = converseSpan;
		}
	}

	public abstract void allocateTransverse(Context context, int ascent, int descent);

	public void addAfter(final Context context, final Brick brick) {
		final Properties properties = brick.properties(context);
		if (properties.split) {
			parent.breakCourse(context, index + 1).add(context, 0, ImmutableList.of(brick));
		} else
			parent.add(context, index + 1, ImmutableList.of(brick));
		ImmutableList.copyOf(attachments).forEach(a -> a.addAfter(context, brick));
	}

	public void addBefore(final Context context, final Brick brick) {
		if (properties(context).split) {
			if (parent.index == 0) {
				parent.add(context, 0, ImmutableList.of(brick));
				parent.breakCourse(context, 1);
			} else {
				if (brick.properties(context).split) {
					final Course previousCourse = parent.parent.children.get(parent.index - 1);
					final int insertIndex = previousCourse.children.size();
					previousCourse.add(context, insertIndex, ImmutableList.of(brick));
					previousCourse.breakCourse(context, insertIndex);
				} else {
					final Course previousCourse = parent.parent.children.get(parent.index - 1);
					previousCourse.add(context, previousCourse.children.size(), ImmutableList.of(brick));
				}
			}
		} else {
			if (index > 0 && brick.properties(context).split) {
				parent.breakCourse(context, index).add(context, 0, ImmutableList.of(brick));
			} else
				parent.add(context, index, ImmutableList.of(brick));
		}
		ImmutableList.copyOf(attachments).forEach(a -> a.addBefore(context, brick));
	}

	public Brick previous() {
		if (index == 0) {
			if (parent.index == 0) {
				return null;
			}
			return last(parent.parent.children.get(parent.index - 1).children);
		}
		return parent.children.get(index - 1);
	}

	public Brick next() {
		if (index + 1 == parent.children.size()) {
			if (parent.index + 1 == parent.parent.children.size()) {
				return null;
			}
			return parent.parent.children.get(parent.index + 1).children.get(0);
		}
		return parent.children.get(index + 1);
	}

	/**
	 * Call when a layout property of the brick has changed (size, alignment)
	 *
	 * @param context
	 */
	public void changed(final Context context) {
		if (parent != null)
			parent.changed(context, index);
	}

	public void addAttachment(final Context context, final Attachment attachment) {
		attachments.add(attachment);
		attachment.setConverse(context, getConverse(context));
		if (parent != null) {
			attachment.setTransverse(context, parent.transverseStart);
			attachment.setTransverseSpan(context, parent.ascent, parent.descent);
		}
	}

	public void removeAttachment(final Context context, final Attachment attachment) {
		attachments.remove(attachment);
	}

	public Set<Attachment> getAttachments(final Context context) {
		return attachments;
	}

	protected void destroyed(final Context context) {
		inter.brickDestroyed(context);
	}

	public void destroy(final Context context) {
		ImmutableSet.copyOf(attachments).forEach(a -> a.destroy(context));
		parent.removeFromSystem(context, index);
		destroyed(context);
	}
}
