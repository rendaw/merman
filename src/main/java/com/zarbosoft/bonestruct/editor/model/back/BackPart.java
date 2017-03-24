package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.internal.Node;

import java.util.Set;

@Configuration
public abstract class BackPart {
	public abstract Node buildBackRule(Syntax syntax, NodeType nodeType);

	protected Parent parent = null;

	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
	}

	final public Path getPath(final Path basis) {
		if (parent.part() == null)
			return basis.add(parent.pathSection());
		return parent.part().getPath(basis).add(parent.pathSection());
	}

	abstract class Parent {
		public abstract BackPart part();

		public abstract String pathSection();
	}
}
