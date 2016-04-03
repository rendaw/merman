package com.zarbosoft.bonestruct;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Pair;

public class Helper {

	public static <T> Stream<T> stream(T[] values) {
		return Arrays.asList(values).stream();
	}

	@FunctionalInterface
	public interface Thrower1<T> {
		T get() throws Throwable;
	}

	@FunctionalInterface
	public interface Thrower2 {
		void get() throws Throwable;
	}
	
	static class UncheckedException extends RuntimeException {
		private static final long serialVersionUID = 9029838186087025315L;
		
		public UncheckedException(Throwable e) {
			super(e);
		}
	}
	
	public static <T> T uncheck(Thrower1<T> code) {
		try {
			return code.get();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new UncheckedException(e);
		}
	}

	public static void uncheck(Thrower2 code) {
		try {
			code.get();
		} catch (RuntimeException e) {
			throw e;
		} catch (Throwable e) {
			throw new UncheckedException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <L, R> void popDoubleList(Store s, int length, Pair.Consumer<L, R> callback) {
		IntStream.range(0, length).forEach(i -> {
			Object l = s.popStack();
			Object r = s.popStack();
			callback.accept((L)l, (R)r);
		});
	}

	@SuppressWarnings("unchecked")
	public static <T> void popSingleList(Store s, Consumer<T> callback) {
		Integer count = (Integer) s.popStack();
		IntStream.range(0, count).forEach(i -> {
			callback.accept((T)s.popStack());
		});
	}

	public static <T> T last(List<T> values) {
		return values.get(values.size() - 1);
	}

}
