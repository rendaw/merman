package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.history.Change;
import com.zarbosoft.bonestruct.history.changes.ChangeRecordKeyAdd;
import com.zarbosoft.bonestruct.history.changes.ChangeRecordKeyRemove;

public class MiddleRecordKey extends MiddlePrimitive {
	@Override
	public Change changeAdd(
			final ValuePrimitive value, final int begin, final String text
	) {
		return new ChangeRecordKeyAdd(value, begin, text);
	}

	@Override
	public Change changeRemove(final ValuePrimitive value, final int begin, final int length) {
		return new ChangeRecordKeyRemove(value, begin, length);
	}

}
