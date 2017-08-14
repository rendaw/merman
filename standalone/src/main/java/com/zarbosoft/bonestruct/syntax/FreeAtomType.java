package com.zarbosoft.bonestruct.syntax;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.BackDataRootArray;
import com.zarbosoft.bonestruct.syntax.back.BackPart;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePart;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.Walk;

import java.util.*;

@Configuration
public class FreeAtomType extends AtomType {
	@Configuration
	public String name;

	@Configuration
	public List<FrontPart> front = new ArrayList<>();

	@Configuration
	public List<BackPart> back = new ArrayList<>();

	@Configuration
	public Map<String, MiddlePart> middle = new HashMap<>();

	@Configuration
	public Map<String, AlignmentDefinition> alignments = new HashMap<>();

	@Configuration(optional = true)
	public int precedence = Integer.MAX_VALUE;

	@Configuration(name = "associate_forward", optional = true)
	public boolean frontAssociative = false;

	@Configuration(name = "auto_choose_ambiguity", optional = true)
	public int autoChooseAmbiguity = -1;

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
		return precedence;
	}

	@Override
	public boolean frontAssociative() {
		return frontAssociative;
	}

	@Override
	public String name() {
		return name;
	}

	public Atom create(final Syntax syntax) {
		final Map<String, Value> data = new HashMap<>();
		middle.entrySet().stream().forEach(e -> data.put(e.getKey(), e.getValue().create(syntax)));
		return new Atom(this, data);
	}

	@Override
	public void finish(
			final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes
	) {
		super.finish(syntax, allTypes, scalarTypes);
		back.forEach(backPart -> {
			if (backPart instanceof BackDataRootArray) {
				throw new InvalidSyntax(String.format(
						"[%s] has back parts of type [%s] which may only be used in the root atom type.",
						Walk.decideName(BackDataRootArray.class)
				));
			}
		});
	}
}
