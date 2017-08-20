package com.zarbosoft.bonestruct.modules;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.interface1.Configuration;

@Configuration
public abstract class Module {

	/**
	 * Since multiple documents may use the same syntax,
	 * modules may be initialized and independently destroyed multiple times
	 * from the same definition.  All state should go in the State object.
	 */
	public abstract static class State {
		public abstract void destroy(Context context);
	}

	public abstract State initialize(Context context);
}
