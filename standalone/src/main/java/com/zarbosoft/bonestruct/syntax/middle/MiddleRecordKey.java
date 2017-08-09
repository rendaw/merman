package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.ValueRecordKey;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "key")
public class MiddleRecordKey extends MiddlePrimitive {
	@Override
	public com.zarbosoft.bonestruct.document.values.Value create(final Syntax syntax) {
		return new ValueRecordKey(this, "");
	}
}
