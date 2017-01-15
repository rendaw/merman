package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodePart;
import javafx.scene.Node;

import java.util.HashSet;
import java.util.Set;

public abstract class Brick {
	public Course parent;
	public int index;
	Set<Attachment> attachments = new HashSet<>();

	public abstract int converseEdge(final Context context);

	public final VisualNode getNode() { // Temp final
		if (getVisual().parent() == null)
			return null;
		return getVisual().parent().getNode();
	}

	public abstract VisualNodePart getVisual();

	public abstract Properties getPropertiesForTagsChange(Context context, VisualNode.TagsChange change);

	public abstract int getConverse(Context context);

	public Context.Hoverable hover(final Context context, final Vector point) {
		return getVisual().hover(context, point);
	}

	/**
	 * @param context
	 * @return A new brick or null (no elements before or brick already exists)
	 */
	public abstract Brick createPrevious(Context context);

	public static class Properties {
		public final boolean broken;
		public final int ascent;
		public final int descent;
		public final Alignment alignment;
		public final int converseSpan;

		public Properties(
				final boolean broken,
				final int ascent,
				final int descent,
				final Alignment alignment,
				final int converseSpan
		) {
			this.broken = broken;
			this.ascent = ascent;
			this.descent = descent;
			this.alignment = alignment;
			this.converseSpan = converseSpan;
		}
	}

	public abstract Properties properties();

	public abstract Node getRawVisual();

	public abstract void setConverse(Context context, int minConverse, int converse);

	/**
	 * @param context
	 * @return A new brick or null (no elements afterward or brick already exists)
	 */
	public abstract Brick createNext(Context context);

	public abstract void allocateTransverse(Context context, int ascent, int descent);

	public void addAfter(final Context context, final Brick brick) {
		if (brick.properties().broken) {
			parent.breakCourse(context, index + 1).add(context, 0, ImmutableList.of(brick));
		} else
			parent.add(context, index + 1, ImmutableList.of(brick));
	}

	public void addBefore(final Context context, final Brick brick) {
		if (index == 0) {
			if (brick.properties().broken || (parent.index == 0 && properties().broken)) {
				parent.add(context, 0, ImmutableList.of(brick));
				parent.breakCourse(context, 1);
			} else if (parent.index == 0) {
				parent.add(context, 0, ImmutableList.of(brick));
			} else {
				final Course previousCourse = parent.parent.children.get(parent.index - 1);
				previousCourse.add(context, previousCourse.children.size(), ImmutableList.of(brick));
			}
		} else {
			if (brick.properties().broken) {
				parent.breakCourse(context, index).add(context, 0, ImmutableList.of(brick));
			} else
				parent.add(context, index, ImmutableList.of(brick));
		}
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
			parent.attachmentsChanged(context, index);
		}
	}

	public void removeAttachment(final Context context, final Attachment attachment) {
		attachments.remove(attachment);
		if (parent != null)
			parent.attachmentsChanged(context, index);
	}

	public Set<Attachment> getAttachments(final Context context) {
		return attachments;
	}

	protected abstract void destroyed(Context context);

	public void destroy(final Context context) {
		for (final Attachment attachment : attachments)
			attachment.destroy(context);
		destroyed(context);
		parent.removeFromSystem(context, index);
	}
}
