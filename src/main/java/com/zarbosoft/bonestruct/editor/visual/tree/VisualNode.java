package com.zarbosoft.bonestruct.editor.visual.tree;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;

public abstract class VisualNode {
	private final Set<Tag> tags = new HashSet<>();

	public VisualNode(final Set<Tag> tags) {
		this.tags.addAll(tags);
	}

	public abstract void setParent(VisualNodeParent parent);

	public abstract VisualNodeParent parent();

	public abstract boolean select(Context context);

	public abstract Brick createFirstBrick(Context context);

	public abstract Brick createLastBrick(Context context);

	public abstract Brick getFirstBrick(Context context);

	public abstract Brick getLastBrick(Context context);

	public boolean isAncestor(final VisualNodePart node) {
		if (parent() == null)
			return false;
		return parent().getTarget().isAncestor(node);
	}

	public Iterator<VisualNode> children() {
		return Iterators.forArray();
	}

	public abstract int spacePriority();

	public abstract boolean canCompact();

	public abstract void compact(Context context);

	public abstract boolean canExpand();

	public abstract void expand(Context context);

	public abstract Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			Context context, TagsChange change
	);

	public Alignment getAlignment(final String alignment) {
		if (parent() != null)
			return parent().getAlignment(alignment);
		return null;
	}

	public abstract void rootAlignments(Context context, Map<String, Alignment> alignments);

	public abstract void destroyBricks(Context context);

	@Configuration
	public interface Tag {
	}

	public static class TagsChange {
		public final PSet<Tag> add;
		public final PSet<Tag> remove;

		public TagsChange() {
			this.add = HashTreePSet.empty();
			this.remove = HashTreePSet.empty();
		}

		public TagsChange(final Set<Tag> add, final Set<Tag> remove) {
			this.add = HashTreePSet.from(add);
			this.remove = HashTreePSet.from(remove);
		}

		public TagsChange add(final Tag tag) {
			return new TagsChange(add.plus(tag), remove.minus(tag));
		}

		public TagsChange remove(final Tag tag) {
			return new TagsChange(remove.plus(tag), add.minus(tag));
		}
	}

	public Set<Tag> tags() {
		return tags;
	}

	public void changeTags(final Context context, final TagsChange tagsChange) {
		tags.removeAll(tagsChange.remove);
		tags.addAll(tagsChange.add);
	}

	@Configuration(name = "type")
	public static class TypeTag implements Tag {
		@Configuration
		public String value;

		public TypeTag() {
		}

		public TypeTag(final String value) {
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof TypeTag && value.equals(((TypeTag) obj).value);
		}

		public String toString() {
			return String.format("type:%s", value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(TypeTag.class.hashCode(), value);
		}
	}

	@Configuration(name = "part")
	public static class PartTag implements Tag {
		@Configuration
		public String value;

		public PartTag() {
		}

		public PartTag(final String value) {
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof PartTag && value.equals(((PartTag) obj).value);
		}

		public String toString() {
			return String.format("part:%s", value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(PartTag.class.hashCode(), value);
		}
	}

	@Configuration(name = "state")
	public static class StateTag implements Tag {
		@Configuration
		public String value;

		public StateTag() {
		}

		public StateTag(final String value) {
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof StateTag && value.equals(((StateTag) obj).value);
		}

		public String toString() {
			return String.format("state:%s", value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(StateTag.class.hashCode(), value);
		}
	}

	@Configuration(name = "free")
	public static class FreeTag implements Tag {
		@Configuration
		public String value;

		public FreeTag() {
		}

		public FreeTag(final String value) {
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof FreeTag && value.equals(((FreeTag) obj).value);
		}

		public String toString() {
			return String.format("free:%s", value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(FreeTag.class.hashCode(), value);
		}
	}
}
