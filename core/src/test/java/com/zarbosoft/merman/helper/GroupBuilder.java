package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;

import java.util.HashSet;
import java.util.Set;

public class GroupBuilder {
	Set<String> subtypes = new HashSet<>();

	public GroupBuilder type(final FreeAtomType type) {
		subtypes.add(type.id());
		return this;
	}

	public Set<String> build() {
		return subtypes;
	}

	public GroupBuilder group(final String group) {
		subtypes.add(group);
		return this;
	}
}
