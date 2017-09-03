package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.syntax.InvalidSyntax;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.front.FrontDataArrayBase;

import java.util.Map;
import java.util.Set;

@Configuration
public abstract class MiddleArrayBase extends MiddlePart {

	public abstract Path getPath(final ValueArray value, final int actualIndex);

	@Configuration
	public String type;

	public FrontDataArrayBase front;

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
