package com.zarbosoft.bonestruct.visual.alignment;

import com.zarbosoft.bonestruct.Luxem;

@Luxem.Configuration(name = "concensus")
public class ConcensusAlignmentDefinition implements AlignmentDefinition {
	@Override
	public Alignment create() {
		return new ConcensusAlignment();
	}
}
