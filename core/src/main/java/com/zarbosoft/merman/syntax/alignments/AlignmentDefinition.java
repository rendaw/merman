package com.zarbosoft.merman.syntax.alignments;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.visual.Alignment;

@Configuration
public interface AlignmentDefinition {
	Alignment create();
}
