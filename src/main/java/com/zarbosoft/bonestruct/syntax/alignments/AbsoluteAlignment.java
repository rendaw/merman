package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.AbsoluteAlignmentImplementation;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "absolute")
public class AbsoluteAlignment implements AlignmentDefinition {
	@Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new AbsoluteAlignmentImplementation(offset);
	}
}
