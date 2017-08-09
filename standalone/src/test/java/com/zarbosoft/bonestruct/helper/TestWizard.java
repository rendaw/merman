package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.MockeryDisplay;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.syntax.Syntax;

import static com.zarbosoft.bonestruct.helper.Helper.buildDoc;

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
