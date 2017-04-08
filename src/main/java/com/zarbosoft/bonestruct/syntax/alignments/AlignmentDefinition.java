package com.zarbosoft.bonestruct.syntax.alignments;

import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.interface1.Configuration;

@Configuration
public interface AlignmentDefinition {
	Alignment create();
}
