package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.history.Change;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.modules.hotkeys.grammar.Node;
import com.zarbosoft.merman.syntax.Syntax;

import java.util.Map;
import java.util.Set;

@Configuration(name = "primitive")
public class MiddlePrimitive extends MiddlePart {

	@Configuration(optional = true)
	public Node validation;

	public Change changeAdd(final ValuePrimitive value, final int begin, final String text) {
		return new ChangePrimitiveAdd(value, begin, text);
	}

	public Change changeRemove(final ValuePrimitive value, final int begin, final int length) {
		return new ChangePrimitiveRemove(value, begin, length);
	}

	public ValuePrimitive get(final Map<String, com.zarbosoft.merman.document.values.Value> data) {
		return (ValuePrimitive) data.get(id);
	}

	@Override
	public void finish(final Set<String> allTypes, final Set<String> scalarTypes) {

	}

	@Override
	public com.zarbosoft.merman.document.values.Value create(final Syntax syntax) {
		return new ValuePrimitive(this, "");
	}

}
