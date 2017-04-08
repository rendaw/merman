package com.zarbosoft.bonestruct.editor;

import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

	@Override
	public String toString() {
		return segments.stream().map(s -> "/" + s).collect(Collectors.joining());
	}

	public List<String> toList() {
		return segments;
	}

	public Path add(final Path path) {
		return new Path(segments.plusAll(path.segments));
	}
}
