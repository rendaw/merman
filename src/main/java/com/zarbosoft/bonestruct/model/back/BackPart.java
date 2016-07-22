package com.zarbosoft.bonestruct.model.back;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.pidgoon.internal.Node;

import java.util.Set;

@Luxem.Configuration
public interface BackPart {
	Node buildLoadRule();

	default void finish(final NodeType nodeType, final Set<String> middleUsed) {
	}
}
