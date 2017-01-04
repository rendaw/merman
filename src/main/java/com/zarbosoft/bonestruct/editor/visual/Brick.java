package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.VisualNodePart;
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

	public abstract void setConverse(Context context, int converse);

	public abstract Brick createNext(Context context);

	public abstract void allocateTransverse(Context context, int ascent, int descent);

	public void addAfter(final Context context, final Brick brick) {
		parent.add(context, index + 1, ImmutableList.of(brick));
	}

	public void changed(final Context context) {
		if (parent != null)
			parent.changed(context, index);
	}

	public void addAttachment(final Context context, final Attachment attachment) {
		attachments.add(attachment);
		attachment.setConverse(context, getConverse(context));
		attachment.setTransverse(context, parent.transverseStart);
		attachment.setTransverseSpan(context, parent.ascent, parent.descent);
		changed(context);
	}

	public void removeAttachment(final Context context, final Attachment attachment) {
		attachments.remove(attachment);
		changed(context);
	}

	public Set<Attachment> getAttachments(final Context context) {
		return attachments;
	}

	public abstract void destroy(Context context);

	public void remove(final Context context) {
		destroy(context);
		parent.remove(context, index, 1);
	}
}
