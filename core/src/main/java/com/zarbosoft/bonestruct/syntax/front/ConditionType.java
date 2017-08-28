package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.interface1.Configuration;

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
