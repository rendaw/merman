package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.ValueRecordKey;
import com.zarbosoft.merman.syntax.Syntax;

@Configuration(name = "key")
public class MiddleRecordKey extends MiddlePrimitive {
	@Override
	public com.zarbosoft.merman.document.values.Value create(final Syntax syntax) {
		return new ValueRecordKey(this, "");
	}
}
