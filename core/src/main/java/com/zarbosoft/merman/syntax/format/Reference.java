package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.interface1.Configuration;

import java.util.Map;

@Configuration(name = "ref")
public class Reference implements Element {
	@Configuration
	public String name;

	@Override
	public String format(final Map<String, Object> data) {
		return data.getOrDefault(name, "BADKEY").toString();
	}
}
