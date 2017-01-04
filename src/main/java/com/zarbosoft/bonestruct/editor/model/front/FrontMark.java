package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Style;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.nodes.bricks.TextBrick;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Pair;
import org.pcollections.HashTreePSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Luxem.Configuration(name = "mark")
public class FrontMark extends FrontConstantPart {

	@Luxem.Configuration
	public String value;

	private class MarkVisual extends VisualNodePart {
		public VisualNodeParent parent;
		private MarkBrick brick = null;

		public MarkVisual(final Set<Tag> tags) {
			super(tags);
		}

		public void setText(final Context context, final String value) {
			if (brick != null)
				brick.setText(context, value);
		}

		@Override
		public void setParent(final VisualNodeParent parent) {
			this.parent = parent;
		}

		@Override
		public VisualNodeParent parent() {
			return parent;
		}

		@Override
		public boolean select(final Context context) {
			return false;
		}

	/*
	@Override
	public Context.Hoverable hover(final Context context, final Vector point) {
		return null;
	}
	*/

		@Override
		public Brick createFirstBrick(final Context context) {
			if (brick != null)
				throw new AssertionError("Brick should be initially empty or cleared after being deleted");
			brick = new MarkBrick(context);
			brick.setText(context, value);
			return brick;
		}

		@Override
		public String debugTreeType() {
			return String.format("raw@%s %s",
					Integer.toHexString(hashCode()),
					brick == null ? "no brick" : brick.getText()
			);
		}

		@Override
		public Brick getFirstBrick(final Context context) {
			return brick;
		}

		@Override
		public Brick getLastBrick(final Context context) {
			return brick;
		}

		@Override
		public void changeTags(final Context context, final TagsChange tagsChange) {
			super.changeTags(context, tagsChange);
			if (brick != null) {
				brick.setStyle(context);
			}
		}

		@Override
		public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
				final Context context, final TagsChange change
		) {
			if (brick == null)
				return Iterables.concat();
			return Arrays.asList(new Pair<Brick, Brick.Properties>(brick,
					brick.getPropertiesForTagsChange(context, change)
			));
		}

		@Override
		public void destroyBricks(final Context context) {
			if (brick != null)
				brick.remove(context);
		}

		private class MarkBrick extends TextBrick {
			private Style.Baked style;
			private Alignment alignment;

			public MarkBrick(final Context context) {
				setStyle(context);
			}

			public void setStyle(final Context context) {
				style = context.getStyle(tags());
				if (alignment != null)
					alignment.listeners.remove(this);
				alignment = MarkVisual.this.getAlignment(style.alignment);
				if (alignment != null)
					alignment.listeners.add(this);
				changed(context);
				super.setStyle(style);
			}

			@Override
			protected Alignment getAlignment(final Style.Baked style) {
				return MarkVisual.this.getAlignment(style.alignment);
			}

			@Override
			protected Style.Baked getStyle() {
				return style;
			}

			@Override
			public Brick createNext(final Context context) {
				return MarkVisual.this.parent.createNextBrick(context);
			}

			@Override
			public void destroy(final Context context) {
				brick = null;
			}

			@Override
			public VisualNodePart getVisual() {
				return MarkVisual.this;
			}

			@Override
			public Properties getPropertiesForTagsChange(
					final Context context, final TagsChange change
			) {
				final Set<Tag> tags = new HashSet<>(tags());
				tags.removeAll(change.remove);
				tags.addAll(change.add);
				return properties(context.getStyle(tags));
			}
		}
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Set<VisualNode.Tag> tags) {
		final MarkVisual out = new MarkVisual(HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
				.plus(new VisualNode.PartTag("mark")));
		out.setText(context, value);
		return out;
	}
}
