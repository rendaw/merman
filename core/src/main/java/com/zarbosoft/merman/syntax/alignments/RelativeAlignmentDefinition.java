package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.alignment.RelativeAlignment;

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
