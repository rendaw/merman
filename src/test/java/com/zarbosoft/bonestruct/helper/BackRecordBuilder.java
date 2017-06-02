package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.back.BackPart;
import com.zarbosoft.bonestruct.syntax.back.BackRecord;

public class BackRecordBuilder {
	BackRecord back = new BackRecord();

	public BackRecordBuilder add(final String key, final BackPart part) {
		back.pairs.put(key, part);
		return this;
	}

	public BackPart build() {
		return back;
	}
}
