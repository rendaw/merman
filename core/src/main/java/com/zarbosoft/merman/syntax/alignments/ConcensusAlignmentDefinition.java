package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.ConcensusAlignment;

@Configuration(name = "concensus")
public class ConcensusAlignmentDefinition implements AlignmentDefinition {
	@Override
	public Alignment create() {
		return new ConcensusAlignment();
	}
}
