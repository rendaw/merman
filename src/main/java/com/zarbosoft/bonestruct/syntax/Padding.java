package com.zarbosoft.bonestruct.syntax;

import com.zarbosoft.interface1.Configuration;

@Configuration
public class Padding {
	@Configuration(name = "converse_start", optional = true,
			description = "Pad the converse start by this many pixels.")
	public int converseStart = 0;
	@Configuration(name = "converse_end", optional = true, description = "Pad the converse end by this many pixels.")
	public int converseEnd = 0;
	@Configuration(name = "transverse_start", optional = true,
			description = "Pad the transverse start by this many pixels.")
	public int transverseStart = 0;
	@Configuration(name = "transverse_end", optional = true,
			description = "Pad the transverse end by this many pixels.")
	public int transverseEnd = 0;
}
