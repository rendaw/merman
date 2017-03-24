package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.interface1.Configuration;

@Configuration
public abstract class Plugin {

	public abstract static class State {
		public abstract void destroy(Context context);
	}

	public abstract State initialize(Context context);
}
