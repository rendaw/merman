package com.zarbosoft.bonestruct;

import org.pcollections.PVector;

public class Path {
	public final PVector<String> sections;

	public Path(final PVector<String> sections) {
		this.sections = sections;
	}

	public Path add(final String section) {
		return new Path(sections.plus(section));
	}
}
