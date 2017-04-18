package com.zarbosoft.bonestruct.history;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.interface1.Configuration;

@Configuration
public abstract class Change {
	public abstract boolean merge(Change other);

	public abstract Change apply(Context context);
}
