package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.luxem.Luxem;

@Luxem.Configuration(name = "concensus")
public class ConcensusAlignmentDefinition implements AlignmentDefinition {
	@Override
	public Alignment create() {
		return new ConcensusAlignment();
	}
}
