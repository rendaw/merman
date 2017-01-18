package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.pidgoon.internal.Node;

import java.util.Set;

@Luxem.Configuration
public interface BackPart {
	Node buildLoadRule(Syntax syntax);

	default void finish(final NodeType nodeType, final Set<String> middleUsed) {
	}
}
