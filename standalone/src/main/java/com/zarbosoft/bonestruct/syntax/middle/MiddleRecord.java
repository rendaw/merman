package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.syntax.back.BackDataKey;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "record")
public class MiddleRecord extends MiddleArrayBase {
	@Override
	public Path getPath(final ValueArray value, final int actualIndex) {
		final Atom element = value.data.get(actualIndex / 2);
		final String segment =
				((ValuePrimitive) element.data.get(((BackDataKey) element.type.back().get(0)).middle)).get();
		return value.getPath().add(segment);
	}
}
