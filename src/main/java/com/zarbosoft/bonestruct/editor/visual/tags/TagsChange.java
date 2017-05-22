package com.zarbosoft.bonestruct.editor.visual.tags;

import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.Set;

public class TagsChange {
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

	public PSet<Tag> apply(final PSet<Tag> tags) {
		return tags.minusAll(remove).plusAll(add);
	}
}
