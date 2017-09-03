package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.syntax.Syntax;

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
