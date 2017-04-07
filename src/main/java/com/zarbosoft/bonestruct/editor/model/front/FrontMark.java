package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.bricks.TextBrick;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "mark")
public class FrontMark extends FrontConstantPart {

	@Configuration
	public String value;

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

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

		@Override
		public Brick createFirstBrick(final Context context) {
			if (brick != null)
				return null;
			brick = new MarkBrick(context);
			brick.setText(context, value);
			return brick;
		}

		@Override
		public Brick createLastBrick(final Context context) {
			return createFirstBrick(context);
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
		public void destroy(final Context context) {
			if (brick != null)
				brick.destroy(context);
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
					alignment.removeListener(context, this);
				alignment = MarkVisual.this.getAlignment(style.alignment);
				if (alignment != null)
					alignment.addListener(context, this);
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
			public Brick createPrevious(final Context context) {
				return MarkVisual.this.parent.createPreviousBrick(context);
			}

			@Override
			public void destroyed(final Context context) {
				brick = null;
				if (alignment != null)
					alignment.removeListener(context, this);
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
