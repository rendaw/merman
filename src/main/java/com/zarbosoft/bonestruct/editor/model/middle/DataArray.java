package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "array")
public class DataArray extends DataArrayBase {
	@Override
	public Path getPath(final Value value, final int actualIndex) {
		return value.getPath().add(String.format("%d", actualIndex));
	}
}
