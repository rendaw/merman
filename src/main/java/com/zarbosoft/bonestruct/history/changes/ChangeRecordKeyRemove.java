package com.zarbosoft.bonestruct.history.changes;

import com.zarbosoft.bonestruct.document.values.ValuePrimitive;

public class ChangeRecordKeyRemove extends ChangePrimitiveRemove {

	public ChangeRecordKeyRemove(final ValuePrimitive data, final int index, final int size) {
		super(data, index, size);
	}
}
