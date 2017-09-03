package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;

@Configuration(name = "array")
public class MiddleArray extends MiddleArrayBase {
	@Override
	public Path getPath(final ValueArray value, final int actualIndex) {
		return value.getPath().add(String.format("%d", actualIndex));
	}
}
