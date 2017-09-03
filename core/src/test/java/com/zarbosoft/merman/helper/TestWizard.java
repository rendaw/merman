package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.MockeryDisplay;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.syntax.Syntax;

import static com.zarbosoft.merman.helper.Helper.buildDoc;

public class TestWizard {
	public final IdleRunner runner;
	public final Context context;
	private final MockeryDisplay display;

	public TestWizard(final Syntax syntax, final Atom... initial) {
		this.runner = new IdleRunner();
		this.context = buildDoc(runner::idleAdd, syntax, initial);
		this.display = (MockeryDisplay) context.display;
		runner.flush();
	}

	public TestWizard resize(final int size) {
		display.setConverseEdge(context, size);
		runner.flush();
		return this;
	}

	public TestWizard resizeTransitive(final int size) {
		display.setTransverseEdge(context, size);
		runner.flush();
		return this;
	}

	public TestWizard sendHIDEvent(final HIDEvent event) {
		display.sendHIDEvent(event);
		return this;
	}
}
