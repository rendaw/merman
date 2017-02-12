package com.zarbosoft.bonestruct.editor.changes;

import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.luxemj.Luxem;

@Luxem.Configuration
public abstract class Change {
	public abstract boolean merge(Change other);

	public abstract Change apply(Context context);

	public abstract DataElement.Value getValue();
}
