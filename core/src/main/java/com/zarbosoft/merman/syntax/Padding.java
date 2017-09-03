package com.zarbosoft.merman.syntax;

import com.zarbosoft.interface1.Configuration;

@Configuration
public class Padding {
	@Configuration(name = "converse_start", optional = true)
	public int converseStart = 0;
	@Configuration(name = "converse_end", optional = true)
	public int converseEnd = 0;
	@Configuration(name = "transverse_start", optional = true)
	public int transverseStart = 0;
	@Configuration(name = "transverse_end", optional = true)
	public int transverseEnd = 0;
}
