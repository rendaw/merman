package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.alignment.RelativeAlignmentImplementation;

@Luxem.Configuration(name = "relative")
public class RelativeAlignment implements AlignmentDefinition {
	@Luxem.Configuration
	public String base;

	@Luxem.Configuration
	public int offset;

	@Override
	public Alignment create() {
		return new RelativeAlignmentImplementation(base, offset);
	}
}
