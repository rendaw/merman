package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.ConcensusAlignment;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "concensus")
public class ConcensusAlignmentDefinition implements AlignmentDefinition {
	@Override
	public Alignment create() {
		return new ConcensusAlignment();
	}
}
