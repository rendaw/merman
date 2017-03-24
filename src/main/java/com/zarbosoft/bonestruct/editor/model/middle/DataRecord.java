package com.zarbosoft.bonestruct.editor.model.middle;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.back.BackDataKey;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "record")
public class DataRecord extends DataArrayBase {
	@Override
	public Path getPath(final DataArrayBase.Value value, final int actualIndex) {
		final Node element = value.get().get(actualIndex / 2);
		final String segment =
				((DataRecordKey.Value) element.data.get(((BackDataKey) element.type.back().get(0)).middle)).get();
		return value.getPath().add(segment);
	}
}
