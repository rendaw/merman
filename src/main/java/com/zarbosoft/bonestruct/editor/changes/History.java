package com.zarbosoft.bonestruct.editor.changes;

import com.zarbosoft.bonestruct.editor.visual.Context;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class History {
	private Instant pastEnd;
	private final Deque<ChangeGroup> past = new ArrayDeque<>();
	private final Deque<ChangeGroup> future = new ArrayDeque<>();

	public static abstract class Listener {
		public abstract void applied(Context context, ChangeGroup change);
	}

	private final Set<Listener> listeners = new HashSet<>();

	public void undo(final Context context) {
		if (past.isEmpty())
			return;
		final ChangeGroup change = past.pollLast();
		for (final Listener listener : listeners)
			listener.applied(context, change);
		future.addLast(change.apply(context));
	}

	public void redo(final Context context) {
		if (future.isEmpty())
			return;
		final ChangeGroup change = future.pollLast();
		for (final Listener listener : listeners)
			listener.applied(context, change);
		past.addLast(change.apply(context));
	}

	public void finishChange() {
		if (past.peekLast().isEmpty())
			return;
		past.addLast(new ChangeGroup());
	}

	public void apply(final Context context, final Change change) {
		future.clear();
		if (past.isEmpty())
			past.addLast(new ChangeGroup());
		final Change reverse = change.apply(context);
		past.peekLast().add(reverse);
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		listeners.remove(listener);
	}
}
