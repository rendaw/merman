package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration(name = "atom")
public class MiddleAtom extends MiddlePart {

	@Configuration
	public String type;

	public ValueAtom get(final Map<String, com.zarbosoft.bonestruct.document.values.Value> data) {
		return (ValueAtom) data.get(id);
	}

	@Override
	public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {
		if (type == null)
			return; // Gaps have null type, take anything
		if (!scalarTypes.contains(type))
			throw new InvalidSyntax(String.format("Unknown type [%s].", type));
	}

	@Override
	public com.zarbosoft.bonestruct.document.values.Value create(final Syntax syntax) {
		return new ValueAtom(this, syntax.gap.create());
	}
}
