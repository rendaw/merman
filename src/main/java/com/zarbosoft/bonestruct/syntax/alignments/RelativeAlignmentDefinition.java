package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.RelativeAlignment;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "relative")
public class RelativeAlignmentDefinition implements AlignmentDefinition {
	@Configuration
	public String base;

	@Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new RelativeAlignment(base, offset);
	}
}
