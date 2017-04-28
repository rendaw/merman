package com.zarbosoft.bonestruct.display;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.style.ModelColor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MockeryDisplay implements Display {
	int edge;
	int transverseEdge;
	List<IntListener> converseEdgeListeners = new ArrayList<>();
	List<IntListener> transverseEdgeListeners = new ArrayList<>();
	List<DisplayNode> nodes = new ArrayList<>();

	@Override
	public Group group() {
		return new MockeryGroup();
	}

	@Override
	public Image image() {
		return new MockeryImage();
	}

	@Override
	public Text text() {
		return new MockeryText();
	}

	@Override
	public Font font(final String font, final int fontSize) {
		return new MockeryFont(fontSize);
	}

	@Override
	public Drawing drawing() {
		return new MockeryDrawing();
	}

	@Override
	public Blank blank() {
		return new MockeryBlank();
	}

	@Override
	public void addMouseExitListener(final Runnable listener) {

	}

	@Override
	public void addMouseMoveListener(final Consumer<Vector> listener) {

	}

	@Override
	public void addHIDEventListener(final Consumer<HIDEvent> listener) {

	}

	@Override
	public void addTypingListener(final Consumer<String> listener) {

	}

	@Override
	public void focus() {

	}

	@Override
	public int edge(final Context context) {
		return edge;
	}

	@Override
	public void addConverseEdgeListener(final IntListener listener) {
		converseEdgeListeners.add(listener);
	}

	@Override
	public int transverseEdge(final Context context) {
		return transverseEdge;
	}

	@Override
	public void addTransverseEdgeListener(final IntListener listener) {
		transverseEdgeListeners.add(listener);
	}

	@Override
	public void add(final int index, final DisplayNode node) {
		nodes.add(index, node);
	}

	@Override
	public int size() {
		return nodes.size();
	}

	@Override
	public void remove(final DisplayNode node) {
		nodes.remove(node);
	}

	@Override
	public void setBackgroundColor(final ModelColor color) {

	}
}
