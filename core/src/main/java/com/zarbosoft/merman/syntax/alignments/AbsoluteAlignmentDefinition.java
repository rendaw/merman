package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.AbsoluteAlignment;

@Configuration(name = "absolute")
public class AbsoluteAlignmentDefinition implements AlignmentDefinition {
	@Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new AbsoluteAlignment(offset);
	}
}
