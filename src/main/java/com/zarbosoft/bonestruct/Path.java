package com.zarbosoft.bonestruct;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Arrays;

public class Path {
	public final PVector<String> segments;

	public Path(final PVector<String> segments) {
		this.segments = segments;
	}

	public Path(final String... segments) {
		this.segments = TreePVector.from(Arrays.asList(segments));
	}

	public Path add(final String section) {
		return new Path(segments.plus(section));
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Path))
			return false;
		if (((Path) obj).segments.size() != segments.size())
			return false;
		for (int i = 0; i < segments.size(); ++i) {
			if (!((Path) obj).segments.get(i).equals(segments.get(i)))
				return false;
		}
		return true;
	}

	public boolean contains(final Path other) {
		if (other.segments.size() > segments.size())
			return false;
		for (int i = 0; i < other.segments.size(); ++i) {
			if (!other.segments.get(i).equals(segments.get(i)))
				return false;
		}
		return true;
	}
}
