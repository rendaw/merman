package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public abstract class MiddleArrayBase extends MiddleElement {

	public abstract Path getPath(final ValueArray value, final int actualIndex);

	@Configuration
	public String type;

	@Override
	public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {
		if (type != null && !allTypes.contains(type))
			throw new InvalidSyntax(String.format("Unknown type [%s].", type));
	}

	@Override
	public Value create(final Syntax syntax) {
		return new ValueArray(this);
	}

	public ValueArray get(final Map<String, Value> data) {
		return (ValueArray) data.get(id);
	}
}
