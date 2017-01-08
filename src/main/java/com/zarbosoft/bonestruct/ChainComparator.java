package com.zarbosoft.bonestruct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ChainComparator<T> {
	@FunctionalInterface
	private interface Step<T> {
		int apply(T o1, T o2);
	}

	private final List<Step<T>> steps = new ArrayList<>();

	public ChainComparator() {
	}

	public ChainComparator<T> trueFirst(final Function<T, Boolean> accessor) {
		steps.add((a, b) -> accessor.apply(b).compareTo(accessor.apply(b)));
		return this;
	}

	public ChainComparator<T> falseFirst(final Function<T, Boolean> accessor) {
		steps.add((a, b) -> accessor.apply(a).compareTo(accessor.apply(b)));
		return this;
	}

	public <R extends Comparable> ChainComparator<T> lesserFirst(final Function<T, R> accessor) {
		steps.add((a, b) -> accessor.apply(a).compareTo(accessor.apply(b)));
		return this;
	}

	public <R extends Comparable> ChainComparator<T> greaterFirst(final Function<T, R> accessor) {
		steps.add((a, b) -> accessor.apply(b).compareTo(accessor.apply(a)));
		return this;
	}

	public Comparator<T> build() {
		return new Comparator<T>() {
			@Override
			public int compare(final T o1, final T o2) {
				int result = 0;
				for (final Step step : steps) {
					result = step.apply(o1, o2);
					if (result != 0)
						return result;
				}
				return 0;
			}
		};
	}
}
