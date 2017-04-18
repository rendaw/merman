package com.zarbosoft.bonestruct.history;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.Context;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class History {
	boolean locked = false;
	private Instant pastEnd;
	private final Deque<ChangeGroup> past = new ArrayDeque<>();
	private final Deque<ChangeGroup> future = new ArrayDeque<>();

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

	private ChangeGroup applyGroup(final Context context, final ChangeGroup group) {
		final ChangeGroup out = (ChangeGroup) group.apply(context);
		for (final Listener listener : listeners)
			listener.applied(context, group);
		return out;
	}

	public void undo(final Context context) {
		try (Closeable lock = lock()) {
			if (past.isEmpty())
				return;
			future.addLast(applyGroup(context, past.pollLast()));
		} catch (final IOException e) {
		}
		if (past.isEmpty())
			modifiedStateListeners.forEach(l -> l.changed(false));
	}

	public void redo(final Context context) {
		final boolean wasModified = isModified();
		try (Closeable lock = lock()) {
			if (future.isEmpty())
				return;
			past.addLast(applyGroup(context, future.pollLast()));
		} catch (final IOException e) {
		}
		if (!wasModified)
			modifiedStateListeners.forEach(l -> l.changed(true));
	}

	public void finishChange() {
		try (Closeable lock = lock()) {
			if (past.peekLast().isEmpty())
				return;
			past.addLast(new ChangeGroup());
		} catch (final IOException e) {
		}
	}

	public void apply(final Context context, final Change change) {
		final boolean wasModified = isModified();
		try (Closeable lock = lock()) {
			future.clear();
			if (past.isEmpty())
				past.addLast(new ChangeGroup());
			final Change reverse = change.apply(context);
			past.peekLast().merge(reverse);
			for (final Listener listener : ImmutableList.copyOf(listeners))
				listener.applied(context, change);
		} catch (final IOException e) {
		}
		if (!wasModified)
			modifiedStateListeners.forEach(l -> l.changed(true));
	}

	public boolean isModified() {
		return past.size() > 1 || !past.peekLast().isEmpty();
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
