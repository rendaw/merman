package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.editor.changes.Change;

public class DataRecordKey extends DataPrimitive {
	@Override
	protected Change changeAdd(
			final Value value, final int begin, final String text
	) {
		return new ChangeAdd(value, begin, text);
	}

	@Override
	protected Change changeRemove(final Value value, final int begin, final int length) {
		return new ChangeRemove(value, begin, length);
	}

	public static class ChangeAdd extends DataPrimitive.ChangeAdd {

		public ChangeAdd(final Value data, final int index, final String value) {
			super(data, index, value);
		}
	}

	public static class ChangeRemove extends DataPrimitive.ChangeRemove {

		public ChangeRemove(final Value data, final int index, final int size) {
			super(data, index, size);
		}
	}
}
