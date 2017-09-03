package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.back.BackDataKey;

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
