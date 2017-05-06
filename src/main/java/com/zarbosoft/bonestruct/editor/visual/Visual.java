package com.zarbosoft.bonestruct.editor.visual;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class Visual {
	private PSet<Tag> tags = HashTreePSet.empty();

	public Visual(final PSet<Tag> tags) {
		this.tags = tags;
	}

	public abstract void setParent(VisualParent parent);

	public abstract VisualParent parent();

	public abstract boolean selectDown(Context context);

	public abstract void select(Context context);

	public abstract void selectUp(Context context);

	public abstract Brick createFirstBrick(Context context);

	public abstract Brick createLastBrick(Context context);

	public abstract Brick getFirstBrick(Context context);

	public abstract Brick getLastBrick(Context context);

	public Iterator<Visual> children() {
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

	public abstract void anchor(Context context, Map<String, Alignment> alignments, int depth);

	public abstract void destroy(Context context);

	public abstract boolean isAt(Value value);

	public void suggestCreateBricks(final Context context) {
		final Brick previousBrick = parent() == null ? null : parent().getPreviousBrick(context);
		final Brick nextBrick = parent() == null ? null : parent().getNextBrick(context);
		if (previousBrick != null && nextBrick != null)
			context.fillFromEndBrick(previousBrick);
	}

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

	public PSet<Tag> tags(final Context context) {
		return tags.plusAll(context.globalTags);
	}

	public void changeTags(final Context context, final TagsChange tagsChange) {
		tags = tags.minusAll(tagsChange.remove).plusAll(tagsChange.add);
		if (context.selection.getVisual() == this)
			context.selectionTagsChanged();
		tagsChanged(context);
	}

	public abstract void tagsChanged(Context context);

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

	@Configuration(name = "global")
	public static class GlobalTag implements Tag {
		@Configuration
		public String value;

		public GlobalTag() {
		}

		public GlobalTag(final String value) {
			this.value = value;
		}

		@Override
		public boolean equals(final Object obj) {
			return obj instanceof GlobalTag && value.equals(((GlobalTag) obj).value);
		}

		public String toString() {
			return String.format("global:%s", value);
		}

		@Override
		public int hashCode() {
			return Objects.hash(GlobalTag.class.hashCode(), value);
		}
	}
}
