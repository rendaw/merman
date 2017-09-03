package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.back.BackPart;
import com.zarbosoft.merman.syntax.back.BackRecord;

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
