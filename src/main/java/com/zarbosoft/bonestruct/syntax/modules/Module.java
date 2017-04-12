package com.zarbosoft.bonestruct.syntax.modules;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.interface1.Configuration;

@Configuration
public abstract class Module {

	public abstract static class State {
		public abstract void destroy(Context context);
	}

	public abstract State initialize(Context context);
}
