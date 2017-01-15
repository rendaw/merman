package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.model.Style;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.raw.RawImage;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.scene.Node;
import org.pcollections.HashTreePSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Luxem.Configuration(name = "image")
public class FrontImage extends FrontConstantPart {

	private class ImageVisual extends VisualNodePart {
		public VisualNodeParent parent;
		private ImageBrick brick = null;

		public ImageVisual(final Set<Tag> tags) {
			super(tags);
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
				throw new AssertionError("Brick should be initially empty or cleared after being deleted");
			brick = new ImageBrick(context);
			return brick;
		}

		@Override
		public Brick createLastBrick(final Context context) {
			return createFirstBrick(context);
		}

		@Override
		public String debugTreeType() {
			return String.format("image@%s", Integer.toHexString(hashCode()));
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
				brick.destroy(context);
		}

		private class ImageBrick extends Brick implements AlignmentListener {
			private final RawImage image;
			private Style.Baked style;
			private Alignment alignment;
			private int minConverse;

			public ImageBrick(final Context context) {
				setStyle(context);
				image = RawImage.create(context, style);
			}

			public void setStyle(final Context context) {
				style = context.getStyle(tags());
				if (alignment != null)
					alignment.removeListener(context, this);
				alignment = FrontImage.ImageVisual.this.getAlignment(style.alignment);
				if (alignment != null)
					alignment.addListener(context, this);
				image.setStyle(style);
				changed(context);
			}

			public Properties properties(final Style.Baked style) {
				return new Properties(style.broken,
						(int) image.transverseSpan(),
						(int) 0,
						ImageVisual.this.getAlignment(style.alignment),
						(int) image.converseSpan()
				);
			}

			@Override
			public Brick createNext(final Context context) {
				return FrontImage.ImageVisual.this.parent.createNextBrick(context);
			}

			@Override
			public Brick createPrevious(final Context context) {
				return FrontImage.ImageVisual.this.parent.createPreviousBrick(context);
			}

			@Override
			public void allocateTransverse(final Context context, final int ascent, final int descent) {
				image.setTransverse(ascent, context.transverseEdge);
			}

			@Override
			public void destroyed(final Context context) {
				brick = null;
				if (alignment != null)
					alignment.removeListener(context, this);
			}

			@Override
			public int converseEdge(final Context context) {
				return image.converseEdge(context.edge);
			}

			@Override
			public VisualNodePart getVisual() {
				return FrontImage.ImageVisual.this;
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

			@Override
			public int getConverse(final Context context) {
				return image.getConverse(context.edge);
			}

			@Override
			public int getMinConverse(final Context context) {
				return minConverse;
			}

			@Override
			public Properties properties() {
				return properties(style);
			}

			@Override
			public Node getRawVisual() {
				return image.getVisual();
			}

			@Override
			public void setConverse(final Context context, final int minConverse, final int converse) {
				this.minConverse = minConverse;
				image.setConverse(converse, context.edge);
			}

			@Override
			public void align(final Context context) {
				changed(context);
			}
		}
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Set<VisualNode.Tag> tags) {
		final ImageVisual out = new ImageVisual(HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
				.plus(new VisualNode.PartTag("image")));
		return out;
	}
}
