package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.display.Group;
import com.zarbosoft.bonestruct.editor.display.MockeryDisplay;
import com.zarbosoft.bonestruct.editor.display.MockeryGroup;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.display.derived.CLayout;
import com.zarbosoft.bonestruct.editor.display.derived.ColumnarTableLayout;
import com.zarbosoft.bonestruct.editor.display.derived.RowLayout;
import com.zarbosoft.rendaw.common.Pair;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDisplayDerived {

	@Test
	public void testColumnarTableLayout() {
		final MockeryDisplay display = new MockeryDisplay();
		final ColumnarTableLayout layout = new ColumnarTableLayout(display, 35);
		{
			final Group leftGroup = display.group();
			final Text left = display.text();
			left.setText(null, "1");
			left.setTransverse(null, 8);
			leftGroup.add(left);
			final Text right = display.text();
			right.setText(null, "aaa");
			layout.add(ImmutableList.of(leftGroup, right));
		}
		{
			final Text left = display.text();
			left.setText(null, "333");
			final Text right = display.text();
			right.setText(null, "bb");
			layout.add(ImmutableList.of(left, right));
		}
		{
			final Text left = display.text();
			left.setText(null, "22");
			final Text right = display.text();
			right.setText(null, "c");
			layout.add(ImmutableList.of(left, right));
		}
		{
			final Text left = display.text();
			left.setText(null, "4444");
			final Text right = display.text();
			right.setText(null, "dddd");
			layout.add(ImmutableList.of(left, right));
		}
		layout.layout(null);
		int index = 0;
		for (final Pair<Integer, Integer> pair : ImmutableList.of(new Pair<>(0, 0),
				new Pair<>(30, 8),
				new Pair<>(0, 18),
				new Pair<>(30, 18),
				new Pair<>(0, 28),
				new Pair<>(30, 28),
				new Pair<>(60, 8),
				new Pair<>(100, 8)
		)) {
			final int index2 = index++;
			assertThat(String.format("for index %s, converse", index2),
					((MockeryGroup) layout.group).get(index2).converse(null),
					equalTo(pair.first)
			);
			assertThat(String.format("for index %s, transverse", index2),
					((MockeryGroup) layout.group).get(index2).transverse(null),
					equalTo(pair.second)
			);
		}
	}

	@Test
	public void testCLayout() {
		final MockeryDisplay display = new MockeryDisplay();
		final CLayout layout = new CLayout(display);
		{
			final Group itemGroup = display.group();
			final Text item = display.text();
			item.setText(null, "dog");
			item.setTransverse(null, 8);
			itemGroup.add(item);
			layout.add(itemGroup);
		}
		{
			final Text item = display.text();
			item.setText(null, "donut");
			layout.add(item);
		}
		{
			final Group itemGroup = display.group();
			final Text item = display.text();
			item.setText(null, "9");
			item.setTransverse(null, 8);
			itemGroup.add(item);
			layout.add(itemGroup);
		}
		{
			final Text item = display.text();
			item.setText(null, "apple");
			layout.add(item);
		}
		layout.layout(null);
		int index = 0;
		for (final Pair<Integer, Integer> pair : ImmutableList.of(new Pair<>(0, 0),
				new Pair<>(30, 0),
				new Pair<>(80, 0),
				new Pair<>(90, 0)
		)) {
			final int index2 = index++;
			assertThat(String.format("for index %s, converse", index2),
					((MockeryGroup) layout.group).get(index2).converse(null),
					equalTo(pair.first)
			);
			assertThat(String.format("for index %s, transverse", index2),
					((MockeryGroup) layout.group).get(index2).transverse(null),
					equalTo(pair.second)
			);
		}
	}

	@Test
	public void testRowLayout() {
		final MockeryDisplay display = new MockeryDisplay();
		final RowLayout layout = new RowLayout(display);
		{
			final Group itemGroup = display.group();
			final Text item = display.text();
			item.setText(null, "dog");
			item.setTransverse(null, 8);
			itemGroup.add(item);
			layout.add(itemGroup);
		}
		{
			final Text item = display.text();
			item.setText(null, "donut");
			layout.add(item);
		}
		{
			final Group itemGroup = display.group();
			final Text item = display.text();
			item.setText(null, "9");
			item.setTransverse(null, 8);
			itemGroup.add(item);
			layout.add(itemGroup);
		}
		{
			final Text item = display.text();
			item.setText(null, "apple");
			layout.add(item);
		}
		layout.layout(null);
		int index = 0;
		for (final Pair<Integer, Integer> pair : ImmutableList.of(new Pair<>(0, 0),
				new Pair<>(30, 8),
				new Pair<>(80, 0),
				new Pair<>(90, 8)
		)) {
			final int index2 = index++;
			assertThat(String.format("for index %s, converse", index2),
					((MockeryGroup) layout.group).get(index2).converse(null),
					equalTo(pair.first)
			);
			assertThat(String.format("for index %s, transverse", index2),
					((MockeryGroup) layout.group).get(index2).transverse(null),
					equalTo(pair.second)
			);
		}
	}
}
