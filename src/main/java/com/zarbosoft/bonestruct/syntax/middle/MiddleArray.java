package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "array")
public class MiddleArray extends MiddleArrayBase {
	@Override
	public Path getPath(final ValueArray value, final int actualIndex) {
		return value.getPath().add(String.format("%d", actualIndex));
	}
}
