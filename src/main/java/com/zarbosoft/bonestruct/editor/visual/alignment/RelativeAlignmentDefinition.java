package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.Luxem;

@Luxem.Configuration(name = "relative")
public class RelativeAlignmentDefinition implements AlignmentDefinition {
	@Luxem.Configuration
	public String base;

	@Luxem.Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new RelativeAlignment(base, offset);
	}
}
