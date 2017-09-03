package com.zarbosoft.merman.modules;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.Context;

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
