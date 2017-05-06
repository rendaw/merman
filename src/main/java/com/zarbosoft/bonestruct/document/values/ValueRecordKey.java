package com.zarbosoft.bonestruct.document.values;

import com.zarbosoft.bonestruct.editor.history.Change;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeRecordKeyAdd;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeRecordKeyRemove;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecordKey;

public class ValueRecordKey extends ValuePrimitive {
	public ValueRecordKey(final MiddleRecordKey middle, final String data) {
		super(middle, data);
	}

	public Change changeRemove(final int begin, final int length) {
		return new ChangeRecordKeyRemove(this, begin, length);
	}

	public Change changeAdd(final int begin, final String text) {
		return new ChangeRecordKeyAdd(this, begin, text);
	}
}
