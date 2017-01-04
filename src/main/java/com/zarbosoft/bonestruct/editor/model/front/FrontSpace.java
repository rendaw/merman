package com.zarbosoft.bonestruct.editor.model.front;

import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.Luxem;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.Style;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.AlignmentListener;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.pcollections.HashTreePSet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Luxem.Configuration(name = "space")
public class FrontSpace extends FrontConstantPart {
	private class SpaceVisual extends VisualNodePart {
		public VisualNodeParent parent;
		SpaceBrick brick;

		public SpaceVisual(final Set<Tag> tags) {
			super(HashTreePSet.from(tags).plus(new PartTag("space")));
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
			if (brick == null)
				brick = new SpaceBrick(context);
			return brick;
		}

		@Override
		public void changeTags(final Context context, final TagsChange tagsChange) {
			super.changeTags(context, tagsChange);
			if (brick != null) {
				brick.setStyle(context);
			}
		}

		private class SpaceBrick extends Brick implements AlignmentListener {
			private int converse = 0;
			private Style.Baked style;
			private Alignment alignment;
			private final Region visual = new Region();

			public SpaceBrick(final Context context) {
				setStyle(context);
			}

			public void setStyle(final Context context) {
				this.style = context.getStyle(SpaceVisual.this.tags());
				if (alignment != null)
					alignment.listeners.remove(this);
				alignment = SpaceVisual.this.getAlignment(style.alignment);
				if (alignment != null)
					alignment.listeners.add(this);
				changed(context);
			}

			@Override
			public int converseEdge(final Context context) {
				return Math.max(Math.min(converse + style.space, context.edge), converse);
			}

			@Override
			public VisualNodePart getVisual() {
				return SpaceVisual.this;
			}

			@Override
			public Properties getPropertiesForTagsChange(final Context context, final TagsChange change) {
				final Set<Tag> tags = new HashSet<>(tags());
				tags.removeAll(change.remove);
				tags.addAll(change.add);
				return properties(context.getStyle(tags));
			}

			@Override
			public Properties properties() {
				return properties(style);
			}

			public Properties properties(final Style.Baked style) {
				return new Properties(style.broken,
						style.spaceTransverseBefore,
						style.spaceTransverseAfter,
						alignment,
						style.space + style.spaceBefore + style.spaceAfter
				);
			}

			@Override
			public Node getRawVisual() {
				return visual;
			}

			@Override
			public void setConverse(final Context context, final int converse) {
				this.converse = converse;
				context.translate(visual, new Vector(converse, 0));
			}

			@Override
			public Brick createNext(final Context context) {
				return SpaceVisual.this.parent.createNextBrick(context);
			}

			@Override
			public void allocateTransverse(final Context context, final int ascent, final int descent) {

			}

			@Override
			public void destroy(final Context context) {
				brick = null;
			}

			@Override
			public void align(final Context context) {
				changed(context);
			}

			@Override
			public int getConverse(final Context context) {
				return converse;
			}
		}
	}

	@Override
	public VisualNodePart createVisual(final Context context, final Set<VisualNode.Tag> tags) {
		return new SpaceVisual(HashTreePSet
				.from(tags)
				.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet())));
	}
}
