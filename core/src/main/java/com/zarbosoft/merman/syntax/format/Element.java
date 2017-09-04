package com.zarbosoft.merman.syntax.format;

import com.zarbosoft.interface1.Configuration;

import java.util.Map;

@Configuration
interface Element {
	String format(Map<String, Object> data);
}
