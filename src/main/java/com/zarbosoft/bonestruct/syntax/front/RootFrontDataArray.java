package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;

public class RootFrontDataArray extends FrontDataArrayBase {
	@Override
	public String middle() {
		return "value";
	}

	public void finish(final MiddleArray root) {
		dataType = root;
	}
}
