package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.AbsoluteAlignment;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "absolute")
public class AbsoluteAlignmentDefinition implements AlignmentDefinition {
	@Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new AbsoluteAlignment(offset);
	}
}
