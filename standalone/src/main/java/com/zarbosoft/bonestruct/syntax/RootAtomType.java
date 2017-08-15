package com.zarbosoft.bonestruct.syntax;

import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.BackPart;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePart;
import com.zarbosoft.interface1.Configuration;

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
	public boolean frontAssociative() {
		return false;
	}

	@Override
	public String name() {
		return "root array";
	}
}
