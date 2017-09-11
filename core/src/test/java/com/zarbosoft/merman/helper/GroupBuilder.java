package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;

import java.util.ArrayList;
import java.util.List;

public class GroupBuilder {
	List<String> subtypes = new ArrayList<>();

	public GroupBuilder type(final FreeAtomType type) {
		subtypes.add(type.id());
		return this;
	}

	public List<String> build() {
		return subtypes;
	}

	public GroupBuilder group(final String group) {
		subtypes.add(group);
		return this;
	}
}
