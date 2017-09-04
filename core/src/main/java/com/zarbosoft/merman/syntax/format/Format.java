package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.interface1.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class Format {
	@Configuration(typeless = Literal.class)
	public List<Element> elements;

	public String format(final Map<String, Object> data) {
		return elements.stream().map(element -> element.format(data)).collect(Collectors.joining());
	}
}
