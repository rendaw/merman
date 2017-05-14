package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;

import java.util.Set;

@Configuration
public abstract class MiddlePart {
	public String id;

	public abstract void finish(Set<String> allTypes, Set<String> scalarTypes);

	public abstract Value create(Syntax syntax);

	// TODO
	/*
	@Configuration(optional = true)
	boolean optional = false;
	 */
}
