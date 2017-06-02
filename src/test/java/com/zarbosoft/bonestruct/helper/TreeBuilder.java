package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.middle.MiddleAtom;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePrimitive;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecordKey;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeBuilder {
	private final AtomType type;
	private final Map<String, Value> data = new HashMap<>();

	public TreeBuilder(final AtomType type) {
		this.type = type;
	}

	public TreeBuilder add(final String key, final TreeBuilder builder) {
		data.put(key, new ValueAtom((MiddleAtom) type.middle().get(key), builder.build()));
		return this;
	}

	public TreeBuilder add(final String key, final Atom atom) {
		data.put(key, new ValueAtom((MiddleAtom) type.middle().get(key), atom));
		return this;
	}

	public TreeBuilder add(final String key, final String text) {
		data.put(key, new ValuePrimitive((MiddlePrimitive) type.middle().get(key), text));
		return this;
	}

	public TreeBuilder addKey(final String key, final String text) {
		data.put(key, new ValuePrimitive((MiddleRecordKey) type.middle().get(key), text));
		return this;
	}

	public TreeBuilder addArray(final String key, final List<Atom> values) {
		data.put(key, new ValueArray(type.getDataArray(key), values));
		return this;
	}

	public TreeBuilder addArray(final String key, final Atom... values) {
		data.put(key, new ValueArray(type.getDataArray(key), Arrays.asList(values)));
		return this;
	}

	public TreeBuilder addRecord(final String key, final Atom... values) {
		data.put(key, new ValueArray(type.getDataRecord(key), Arrays.asList(values)));
		return this;
	}

	public Atom build() {
		return new Atom(type, data);
	}

	public Value buildArray() {
		return new ValueArray(null, Arrays.asList(new Atom(type, data)));
	}

}
