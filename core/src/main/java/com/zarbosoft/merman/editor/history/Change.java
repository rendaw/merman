package com.zarbosoft.merman.editor.history;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.Context;

@Configuration
public abstract class Change {
	public abstract boolean merge(Change other);

	public abstract Change apply(Context context);
}
