package com.zarbosoft.bonestruct.editor.history.changes;

import com.zarbosoft.bonestruct.document.values.ValuePrimitive;

public class ChangeRecordKeyAdd extends ChangePrimitiveAdd {

	public ChangeRecordKeyAdd(final ValuePrimitive data, final int index, final String value) {
		super(data, index, value);
	}
}
