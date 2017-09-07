package com.zarbosoft.merman;

import com.zarbosoft.merman.editor.display.javafx.OverlayList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestOverlayList {
	private class Wizard {
		final List<Integer> base;
		final OverlayList<Integer> overlay;

		Wizard(final Integer... initial) {
			base = new ArrayList<>(Arrays.asList(initial));
			overlay = new OverlayList<>(base);
		}

		public Wizard add(final int index, final Integer... values) {
			overlay.add(index, Arrays.asList(values));
			return this;
		}

		public Wizard remove(final int index, final int count) {
			overlay.remove(index, count);
			return this;
		}

		public void check(final Integer... values) {
			overlay.flush();
			assertThat(base, equalTo(Arrays.asList(values)));
		}
	}

	@Test
	public void addBlank() {
		new Wizard().add(0, 0, 1, 2).check(0, 1, 2);
	}

	@Test
	public void addBeginning() {
		new Wizard(0, 1, 2).add(0, 7, 8).check(7, 8, 0, 1, 2);
	}

	@Test
	public void addMiddle() {
		new Wizard(0, 1, 2).add(1, 7, 8).check(0, 7, 8, 1, 2);
	}

	@Test
	public void addTwiceBeginning() {
		new Wizard(0, 1, 2).add(1, 7, 8).add(1, 9).check(0, 9, 7, 8, 1, 2);
	}

	@Test
	public void addTwiceMiddle() {
		new Wizard(0, 1, 2).add(1, 7, 8).add(2, 9).check(0, 7, 9, 8, 1, 2);
	}

	@Test
	public void addTwiceEnd() {
		new Wizard(0, 1, 2).add(1, 7, 8).add(3, 9).check(0, 7, 8, 9, 1, 2);
	}

	@Test
	public void addTwiceSpaced() {
		new Wizard(0, 1, 2).add(0, 7, 8).add(4, 9, 11).check(7, 8, 0, 1, 9, 11, 2);
	}

	@Test
	public void addEnd() {
		new Wizard(0, 1, 2).add(3, 7, 8).check(0, 1, 2, 7, 8);
	}

	@Test
	public void removeBeginning() {
		new Wizard(0, 1, 2).remove(0, 2).check(2);
	}

	@Test
	public void removeMiddle() {
		new Wizard(0, 1, 2).remove(1, 1).check(0, 2);
	}

	@Test
	public void removeTwice() {
		new Wizard(0, 1, 2, 3, 4).remove(1, 2).remove(1, 1).check(0, 4);
	}

	@Test
	public void removeEnd() {
		new Wizard(0, 1, 2).remove(2, 1).check(0, 1);
	}

	@Test
	public void addRemove() {
		new Wizard(0, 1, 2).add(2, 7, 8, 9).remove(3, 1).check(0, 1, 7, 9, 2);
	}

	@Test
	public void removeAddBeginningExact() {
		new Wizard(0, 1, 2).add(2, 7, 8, 9).remove(2, 1).check(0, 1, 8, 9, 2);
	}

	@Test
	public void removeAddBeginningLoose() {
		new Wizard(0, 1, 2).add(2, 7, 8, 9).remove(1, 2).check(0, 8, 9, 2);
	}

	@Test
	public void removeAddEndExact() {
		new Wizard(0, 1, 2).add(2, 7, 8, 9).remove(4, 1).check(0, 1, 7, 8, 2);
	}

	@Test
	public void removeAddEndLoose() {
		new Wizard(0, 1, 2).add(1, 7, 8, 9).remove(3, 2).check(0, 7, 8, 2);
	}

	@Test
	public void removeAddWholeExact() {
		new Wizard(0, 1, 2).add(1, 7, 8).remove(1, 2).check(0, 1, 2);
	}

	@Test
	public void removeAddWholeLoose() {
		new Wizard(0, 1, 2, 3).add(2, 7, 8).remove(1, 4).check(0, 3);
	}

	@Test
	public void removeMultiple() {
		new Wizard(0, 1, 2).add(1, 7, 8).add(4, 9, 11).remove(0, 7).check();
	}
}
