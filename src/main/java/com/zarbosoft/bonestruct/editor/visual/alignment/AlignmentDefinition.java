package com.zarbosoft.bonestruct.editor.visual.alignment;

import com.zarbosoft.bonestruct.editor.luxem.Luxem;

@Luxem.Configuration
public interface AlignmentDefinition {
	Alignment create();
}
