package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.luxemj.Luxem;

@Luxem.Configuration(name = "array")
public class FrontDataArray extends FrontDataArrayBase {

	@Luxem.Configuration
	public String middle;

	@Override
	public String middle() {
		return middle;
	}
}
