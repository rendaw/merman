package com.zarbosoft.merman.document.values;

import com.zarbosoft.merman.editor.history.Change;
import com.zarbosoft.merman.editor.history.changes.ChangeRecordKeyAdd;
import com.zarbosoft.merman.editor.history.changes.ChangeRecordKeyRemove;
import com.zarbosoft.merman.syntax.middle.MiddleRecordKey;

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
