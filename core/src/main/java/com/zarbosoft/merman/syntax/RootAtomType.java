package com.zarbosoft.merman.syntax;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackPart;
import com.zarbosoft.merman.syntax.front.FrontPart;
import com.zarbosoft.merman.syntax.middle.MiddlePart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class RootAtomType extends AtomType {
	@Configuration
	public List<FrontPart> front = new ArrayList<>();

	@Configuration
	public List<BackPart> back = new ArrayList<>();

	@Configuration
	public Map<String, MiddlePart> middle = new HashMap<>();

	@Configuration
	public Map<String, AlignmentDefinition> alignments = new HashMap<>();

	@Override
	public String id() {
		return "root";
	}

	@Override
	public int depthScore() {
		return 0;
	}

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, MiddlePart> middle() {
		return middle;
	}

	@Override
	public List<BackPart> back() {
		return back;
	}

	@Override
	public Map<String, AlignmentDefinition> alignments() {
		return alignments;
	}

	@Override
	public int precedence() {
		return -Integer.MAX_VALUE;
	}

	@Override
	public boolean associateForward() {
		return false;
	}

	@Override
	public String name() {
		return "root array";
	}

	public Atom create(final Syntax syntax) {
		final Map<String, Value> data = new HashMap<>();
		middle.entrySet().stream().forEach(e -> data.put(e.getKey(), e.getValue().create(syntax)));
		return new Atom(this, data);
	}
}
