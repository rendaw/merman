package com.zarbosoft.bonestruct.editor.history;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.SelectionState;
import com.zarbosoft.rendaw.common.DeadCode;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static com.zarbosoft.rendaw.common.Common.last;

public class History {
	boolean locked = false;
	private Instant pastEnd;
	private final Deque<Level> past = new ArrayDeque<>();
	private final Deque<Level> future = new ArrayDeque<>();

	private static class Level extends Change {
		public final List<Change> subchanges = new ArrayList<>();
		private SelectionState select;

		@Override
		public boolean merge(final Change other) {
			if (subchanges.isEmpty()) {
				subchanges.add(other);
			} else if (last(subchanges).merge(other)) {
			} else
				subchanges.add(other);
			return true;
		}

		@Override
		public Change apply(final Context context) {
			final Level out = new Level();
			out.select = context.selection.saveState();
			for (final Change change : Lists.reverse(subchanges)) {
				out.subchanges.add(change.apply(context));
			}
			select.select(context);
			return out;
		}

		public boolean isEmpty() {
			return subchanges.isEmpty();
		}
	}

	public static abstract class Listener {
		public abstract void applied(Context context, Change change);
	}

	@FunctionalInterface
	public interface ModifiedStateListener {
		void changed(boolean modified);
	}

	private final Set<Listener> listeners = new HashSet<>();
	private final Set<ModifiedStateListener> modifiedStateListeners = new HashSet<>();

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

	private Level applyLevel(final Context context, final Level group) {
		final Level out = (Level) group.apply(context);
		for (final Listener listener : listeners)
			listener.applied(context, group);
		return out;
	}

	public boolean undo(final Context context) {
		try (Closeable lock = lock()) {
			if (past.isEmpty())
				return false;
			future.addLast(applyLevel(context, past.pollLast()));
		} catch (final IOException e) {
		}
		if (past.isEmpty())
			modifiedStateListeners.forEach(l -> l.changed(false));
		return true;
	}

	public boolean redo(final Context context) {
		final boolean wasModified = isModified();
		try (Closeable lock = lock()) {
			if (future.isEmpty())
				return false;
			past.addLast(applyLevel(context, future.pollLast()));
		} catch (final IOException e) {
		}
		if (!wasModified)
			modifiedStateListeners.forEach(l -> l.changed(true));
		return true;
	}

	public void finishChange(final Context context) {
		try (Closeable lock = lock()) {
			if (!past.isEmpty() && past.peekLast().isEmpty())
				return;
			past.addLast(new Level());
		} catch (final IOException e) {
		}
	}

	public void apply(final Context context, final Change change) {
		final boolean wasModified = isModified();
		try (Closeable lock = lock()) {
			future.clear();
			final Level reverseLevel;
			if (past.isEmpty()) {
				reverseLevel = new Level();
				past.addLast(reverseLevel);
			} else
				reverseLevel = past.peek();
			if (reverseLevel.select == null && context.selection != null)
				reverseLevel.select = context.selection.saveState();
			final Change reverse = change.apply(context);
			reverseLevel.merge(reverse);
			for (final Listener listener : ImmutableList.copyOf(listeners))
				listener.applied(context, change);
		} catch (final IOException e) {
			throw new DeadCode();
		}
		if (!wasModified)
			modifiedStateListeners.forEach(l -> l.changed(true));
	}

	public boolean isModified() {
		if (past.isEmpty())
			return false;
		return past.size() > 1 || !past.peekLast().isEmpty();
	}

	public void clear() {
		past.clear();
		future.clear();
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}

	public void addModifiedStateListener(final ModifiedStateListener listener) {
		modifiedStateListeners.add(listener);
	}
}
