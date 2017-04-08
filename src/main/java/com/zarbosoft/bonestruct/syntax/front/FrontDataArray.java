package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.interface1.Configuration;

@Configuration(name = "array")
public class FrontDataArray extends FrontDataArrayBase {

	@Configuration
	public String middle;

	@Override
	public String middle() {
		return middle;
	}
}
