package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.syntax.middle.MiddleArray;

@Configuration
public class FrontDataRootArray extends FrontDataArrayBase {
	@Override
	public String middle() {
		return "value";
	}

	public void finish(final MiddleArray root) {
		dataType = root;
	}
}
