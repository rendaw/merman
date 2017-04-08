package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration(name = "node")
public class MiddleNode extends MiddleElement {

	@Configuration
	public String type;

	public ValueNode get(final Map<String, com.zarbosoft.bonestruct.document.values.Value> data) {
		return (ValueNode) data.get(id);
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
		return new ValueNode(this, syntax.gap.create());
	}
}
