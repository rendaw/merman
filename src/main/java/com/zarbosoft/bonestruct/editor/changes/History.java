package com.zarbosoft.bonestruct.editor.changes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static com.zarbosoft.rendaw.common.Common.last;

public class History {
	boolean locked = false;
	private Instant pastEnd;
	private final Deque<List<Change>> past = new ArrayDeque<>();
	private final Deque<List<Change>> future = new ArrayDeque<>();

	public static abstract class Listener {
		public abstract void applied(Context context, Change change);
	}

	private final Set<Listener> listeners = new HashSet<>();

	private Closeable lock() {
		if (locked)
			throw new AssertionError("History callback is modifying history.");
		locked = true;
		return new Closeable() {
			@Override
			public void close() throws IOException {
				locked = false;
			}
		};
	}

	private List<Change> applyGroup(final Context context, final List<Change> group) {
		final List<Change> out = new ArrayList<>();
		for (final Change change : Lists.reverse(group)) {
			out.add(change.apply(context));
			for (final Listener listener : listeners)
				listener.applied(context, change);
		}
		return out;
	}

	public void undo(final Context context) {
		try (Closeable lock = lock()) {
			if (past.isEmpty())
				return;
			future.addLast(applyGroup(context, past.pollLast()));
		} catch (final IOException e) {
		}
	}

	public void redo(final Context context) {
		try (Closeable lock = lock()) {
			if (future.isEmpty())
				return;
			past.addLast(applyGroup(context, future.pollLast()));
		} catch (final IOException e) {
		}
	}

	public void finishChange() {
		try (Closeable lock = lock()) {
			if (past.peekLast().isEmpty())
				return;
			past.addLast(new ArrayList<>());
		} catch (final IOException e) {
		}
	}

	public void apply(final Context context, final Change change) {
		try (Closeable lock = lock()) {
			future.clear();
			if (past.isEmpty())
				past.addLast(new ArrayList<>());
			final Change reverse = change.apply(context);
			final List<Change> subchanges = past.peekLast();
			if (subchanges.isEmpty() || !last(subchanges).merge(change))
				subchanges.add(reverse);
			for (final Listener listener : ImmutableList.copyOf(listeners))
				listener.applied(context, change);
		} catch (final IOException e) {
		}
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}
}
