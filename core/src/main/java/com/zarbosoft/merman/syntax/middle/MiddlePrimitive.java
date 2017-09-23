package com.zarbosoft.merman.syntax.middle;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.history.Change;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveAdd;
import com.zarbosoft.merman.editor.history.changes.ChangePrimitiveRemove;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.middle.primitive.Pattern;

import java.util.Map;
import java.util.Set;

@Configuration(name = "primitive")
public class MiddlePrimitive extends MiddlePart {

	@Configuration(optional = true)
	public Pattern pattern = null;

	public Pattern.Matcher matcher = null;

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
		if (pattern != null)
			matcher = pattern.new Matcher();
	}

	@Override
	public com.zarbosoft.merman.document.values.Value create(final Syntax syntax) {
		return new ValuePrimitive(this, "");
	}
}
