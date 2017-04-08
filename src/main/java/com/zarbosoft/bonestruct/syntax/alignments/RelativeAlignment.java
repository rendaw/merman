package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.RelativeAlignmentImplementation;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "relative")
public class RelativeAlignment implements AlignmentDefinition {
	@Configuration
	public String base;

	@Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new RelativeAlignmentImplementation(base, offset);
	}
}
