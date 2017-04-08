package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.ConcensusAlignmentImplementation;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "concensus")
public class ConcensusAlignment implements AlignmentDefinition {
	@Override
	public Alignment create() {
		return new ConcensusAlignmentImplementation();
	}
}
