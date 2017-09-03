package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.condition.ConditionAttachment;

@Configuration
public abstract class ConditionType {
	@Configuration(optional = true)
	public boolean invert = false;

	public abstract ConditionAttachment create(Context context, Atom atom);

	public boolean defaultOn() {
		return invert ? defaultOnImplementation() : !defaultOnImplementation();
	}

	protected abstract boolean defaultOnImplementation();
}
