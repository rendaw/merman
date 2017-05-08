package com.zarbosoft.bonestruct.syntax.middle;

import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.history.Change;
import com.zarbosoft.bonestruct.editor.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.bonestruct.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration(name = "primitive")
public class MiddlePrimitive extends MiddleElement {

	@Configuration(optional = true, description = "An expression grammar describing valid primitive contents.")
	public Node validation;

	public Change changeAdd(final ValuePrimitive value, final int begin, final String text) {
		return new ChangePrimitiveAdd(value, begin, text);
	}

	public Change changeRemove(final ValuePrimitive value, final int begin, final int length) {
		return new ChangePrimitiveRemove(value, begin, length);
	}

	public ValuePrimitive get(final Map<String, com.zarbosoft.bonestruct.document.values.Value> data) {
		return (ValuePrimitive) data.get(id);
	}

	@Override
	public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {

	}

	@Override
	public com.zarbosoft.bonestruct.document.values.Value create(final Syntax syntax) {
		return new ValuePrimitive(this, "");
	}

}
