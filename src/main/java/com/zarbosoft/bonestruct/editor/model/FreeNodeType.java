package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.interface1.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class FreeNodeType extends NodeType {
	@Configuration
	public String name;

	@Configuration
	public List<FrontPart> front = new ArrayList<>();

	@Configuration
	public List<BackPart> back = new ArrayList<>();

	@Configuration
	public Map<String, DataElement> middle = new HashMap<>();

	@Configuration
	public Map<String, AlignmentDefinition> alignments = new HashMap<>();

	@Configuration(optional = true, description = "If this is an operator, the operator precedence.  This is " +
			"used when filling in a suffix gap to raise the new node to the appropriate level.  This is also used " +
			"when wrapping lines - lower precedence nodes will be compacted first, expanded last.")
	public int precedence = 0;

	@Configuration(name = "associate-forward", optional = true, description =
			"If this is an operator, the operator associativity.  This is used when filling in a " +
					"suffix gap to raise the new node to the appropriate level.  If two operators have the same " +
					"precedence, if the higher operator is back associative the lower operator will not be raised.")
	public boolean frontAssociative = false;

	@Configuration(name = "auto-choose-ambiguity", optional = true,
			description = "If this type is a suggestion and there are less than this many choices, auto-choose this type.")
	public int autoChooseAmbiguity = -1;

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, DataElement> middle() {
		return middle;
	}

	@Override
	public List<BackPart> back() {
		return back;
	}

	@Override
	protected Map<String, AlignmentDefinition> alignments() {
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
}
