package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.middle.DataArray;

public class RootFrontDataArray extends FrontDataArrayBase {
	@Override
	public String middle() {
		return "value";
	}

	public void finish(final DataArray root) {
		dataType = root;
	}
}
