package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.interface1.Configuration;

import java.util.Map;

@Configuration(name = "lit")
public class Literal implements Element {
	@Configuration
	public String value;

	@Override
	public String format(final Map<String, Object> data) {
		return value;
	}
}
