package com.zarbosoft.merman.editor.display.javafx;

import com.google.common.collect.Lists;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.List;

public class OverlayList<T> {
	private final List<T> base;
	private final ArrayList<T> overlay;
	private final ArrayList<Pair<Integer, Change>> changes = new ArrayList<>();

	public OverlayList(final List<T> base) {
		this.base = base;
		overlay = new ArrayList<>(base);
		flush();
	}

	private enum Change {
		OK,
		ADD,
		REMOVE
	}

	public void add(final int index, final List<T> data) {
		overlay.addAll(index, data);
		int at = 0;
		for (int changeIndex = 0; changeIndex < changes.size(); ++changeIndex) {
			final Pair<Integer, Change> change = changes.get(changeIndex);
			switch (change.second) {
				case OK: {
					if (index >= at + change.first) {
						at += change.first;
						continue;
					}
					final int offset = index - at;
					if (offset == 0) {
						changes.add(changeIndex, new Pair<>(data.size(), Change.ADD));
						changeIndex += 1;
					} else {
						final int remainder = change.first - offset;
						change.first = offset;
						changeIndex += 1;
						changes.add(changeIndex, new Pair<>(data.size(), Change.ADD));
						changeIndex += 1;
						changes.add(changeIndex, new Pair<>(remainder, Change.OK));
					}
					return;
				}
				case ADD: {
					if (index > at + change.first) {
						at += change.first;
						continue;
					}
					change.first += 1;
					return;
				}
				case REMOVE: {
					break;
				}
			}
		}
		changes.add(new Pair<>(data.size(), Change.ADD));
	}

	public void remove(final T element) {
		final int index = overlay.indexOf(element);
		if (index == -1)
			return;
		remove(index, 1);
	}

	public void remove(final int index, int count) {
		overlay.subList(index, index + count).clear();
		int at = 0;
		for (int changeIndex = 0; changeIndex < changes.size() && count > 0; ++changeIndex) {
			final Pair<Integer, Change> change = changes.get(changeIndex);
			switch (change.second) {
				case OK: {
					if (index > at + change.first) {
						at += change.first;
						continue;
					}
					final int offset = index - at;
					final int follow = change.first - offset;
					final int remove = Math.min(count, follow);
					if (offset == 0) {
						if (changeIndex > 0 && changes.get(changeIndex - 1).second == Change.REMOVE) {
							changes.get(changeIndex - 1).first += remove;
						} else {
							changes.add(changeIndex, new Pair<>(remove, Change.REMOVE));
							changeIndex += 1;
						}
						if (remove == follow) {
							changes.remove(changeIndex);
							changeIndex -= 1;
						} else {
							change.first -= remove;
						}
					} else {
						change.first = offset;
						changeIndex += 1;
						changes.add(changeIndex, new Pair<>(remove, Change.REMOVE));
						if (remove == follow) {

						} else {
							changeIndex += 1;
							changes.add(changeIndex, new Pair<>(follow - remove, Change.OK));
						}
					}
					count -= remove;
					at += offset + follow - remove;
					break;
				}
				case ADD: {
					if (index > at + change.first) {
						at += change.first;
						continue;
					}
					final int offset = index - at;
					final int follow = change.first - offset;
					final int remove = Math.min(count, follow);
					change.first -= remove;
					if (change.first == 0) {
						changes.remove(changeIndex);
						changeIndex -= 1;
					}
					count -= remove;
					at += offset + follow - remove;
					break;
				}
				case REMOVE: {
					break;
				}
			}
		}
	}

	public void clear() {
		remove(0, overlay.size());
		overlay.clear();
	}

	public void flush() {
		int overlayAt = overlay.size();
		int baseAt = base.size();
		for (final Pair<Integer, Change> change : Lists.reverse(changes)) {
			switch (change.second) {
				case OK:
					baseAt -= change.first;
					overlayAt -= change.first;
					break;
				case ADD:
					overlayAt -= change.first;
					base.addAll(baseAt, overlay.subList(overlayAt, overlayAt + change.first));
					break;
				case REMOVE:
					baseAt -= change.first;
					base.subList(baseAt, baseAt + change.first).clear();
					break;
			}
		}
		changes.clear();
		changes.add(new Pair<>(overlay.size(), Change.OK));
	}

	public int size() {
		return overlay.size();
	}

	public T get(final int index) {
		return overlay.get(index);
	}
}
