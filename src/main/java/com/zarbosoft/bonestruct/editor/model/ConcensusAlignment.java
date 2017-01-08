package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.alignment.ConcensusAlignmentImplementation;

@Luxem.Configuration(name = "concensus")
public class ConcensusAlignment implements AlignmentDefinition {
	@Override
	public Alignment create() {
		return new ConcensusAlignmentImplementation();
	}
}
